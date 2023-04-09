package martandr.phinixplus.emu.cpu;

import java.util.ArrayList;
import java.util.function.BiConsumer;

import martandr.phinixplus.emu.Main;

public class CPU {
	public class State {
		public int[] primary_regfile = new int[16];
		public int[] secondary_regfile = new int[16];
		public int reg_ip, reg_jp, reg_rf, reg_st;
		
		public boolean skip = false;
	}
	
	private static final ArrayList<BiConsumer<Integer, State>> impls;
	private static final int[] double_word = {
			0x00004000, 0x00000000, 0xAAAAC00C, 0xA0000000,
			0x00000000, 0x00000000, 0xFFFF0000, 0x0000F0F0
	};

	private State state;
	public State getState() { return state; }
	
	private Addressable memory;
	public Addressable getMemory() { return memory; }
	
	private Addressable io_space;
	public Addressable getIO() { return io_space; }
	
	public CPU(Addressable memory, Addressable io_space) {
		state = new State();
		this.memory = memory;
		this.io_space = io_space;
	}
	
	public void execute() {
		int instr_word = memory.read(state.reg_ip++);
		int opc = getOpc(instr_word);
		if(state.skip) {
			boolean is_double = (double_word[opc>>5] & (1<<(opc&31))) != 0;
			if(is_double) state.reg_ip++;
			state.skip = false;
		} else {
			impls.get(opc).accept(instr_word, state);
			state.primary_regfile[0] = 0;
		}
	}
	
	// Instruction implementations
	static {
		// Array list init and invalid instruction placeholder
		impls = new ArrayList<>(256);
		BiConsumer<Integer, State> invalid = (i, s) -> System.err.println("[ERR @"+Main.asHex(s.reg_ip-1)+"] Invalid instruction");
		
		/* BEGIN INSTRUCTION IMPLEMENTATIONS */
		BiConsumer<Integer, State> sig = (i, s) -> s.reg_st ^= 1<<getIms(i);
		BiConsumer<Integer, State> movxx = (i, s) -> s.primary_regfile[getDst(i)] = s.primary_regfile[getSrc(i)];
		BiConsumer<Integer, State> movyx = (i, s) -> s.secondary_regfile[getDst(i)] = s.primary_regfile[getSrc(i)];
		BiConsumer<Integer, State> movxy = (i, s) -> s.primary_regfile[getDst(i)] = s.secondary_regfile[getSrc(i)];
		BiConsumer<Integer, State> movyy = (i, s) -> s.secondary_regfile[getDst(i)] = s.secondary_regfile[getSrc(i)];
		BiConsumer<Integer, State> lst = (i, s) -> s.reg_st = s.primary_regfile[getSrc(i)];
		BiConsumer<Integer, State> sst = (i, s) -> s.primary_regfile[getDst(i)] = s.reg_st;
		BiConsumer<Integer, State> lrf = (i, s) -> s.reg_rf = s.primary_regfile[getSrc(i)];
		BiConsumer<Integer, State> srf = (i, s) -> s.primary_regfile[getDst(i)] = s.reg_rf;
		BiConsumer<Integer, State> ljp = (i, s) -> s.reg_jp = s.primary_regfile[getSrc(i)];
		BiConsumer<Integer, State> sjp = (i, s) -> s.primary_regfile[getDst(i)] = s.reg_jp;
		BiConsumer<Integer, State> lip = (i, s) -> s.reg_ip = s.reg_jp;
		BiConsumer<Integer, State> sip = (i, s) -> s.primary_regfile[getDst(i)] = s.reg_ip+getIms(i);
		BiConsumer<Integer, State> jmpo = (i, s) -> s.reg_ip += sxt8(getIml(i))-1;
		BiConsumer<Integer, State> jnl = (i, s) -> {
			s.primary_regfile[getDst(i)] = s.reg_ip+1;
			s.reg_ip = s.primary_regfile[getSrc(i)]+getImx();
		};
		BiConsumer<Integer, State> prdr = (i, s) -> s.skip = !Main.isSet(s.reg_rf, getIms(i));
		
		BiConsumer<Integer, State> prdc = (i, s) -> s.skip = !evalCond(getOpc(i), s);
		BiConsumer<Integer, State> prdp = (i, s) -> s.skip = !evalProp(getOpc(i), s.primary_regfile[getDst(i)], s);
		
		BiConsumer<Integer, State> rbcc = (i, s) -> s.reg_rf &= ~(evalCond(getOpc(i), s) ? 0 : 1<<getIms(i));
		BiConsumer<Integer, State> rbcp = (i, s) -> s.reg_rf &= ~(evalProp(getOpc(i), s.primary_regfile[getDst(i)], s) ? 0 : 1<<getIms(i));
		
		BiConsumer<Integer, State> rbdc = (i, s) -> s.reg_rf |= (evalCond(getOpc(i), s) ? 1<<getIms(i) : 0);
		BiConsumer<Integer, State> rbdp = (i, s) -> s.reg_rf |= (evalProp(getOpc(i), s.primary_regfile[getDst(i)], s) ? 1<<getIms(i) : 0);
		
		BiConsumer<Integer, State> addrx = (i, s) -> {
			int a = s.primary_regfile[getDst(i)];
			int b = s.primary_regfile[getSrc(i)];
			int sum = a+b;
			setFlags(s, isNeg(sum), isOvf(a, b, sum), (sum&65536) != 0, isZero(sum));
			s.primary_regfile[getDst(i)] = sum&65535;
		};
		BiConsumer<Integer, State> addry = (i, s) -> {
			int a = s.secondary_regfile[getDst(i)];
			int b = s.primary_regfile[getSrc(i)];
			int sum = a+b;
			setFlags(s, isNeg(sum), isOvf(a, b, sum), (sum&65536) != 0, isZero(sum));
			s.secondary_regfile[getDst(i)] = sum&65535;
		};
		BiConsumer<Integer, State> addix = (i, s) -> {
			int a = s.primary_regfile[getSrc(i)];
			int b = getImx();
			int sum = a+b;
			setFlags(s, isNeg(sum), isOvf(a, b, sum), (sum&65536) != 0, isZero(sum));
			s.primary_regfile[getDst(i)] = sum&65535;
		};
		BiConsumer<Integer, State> addiy = (i, s) -> {
			int a = s.secondary_regfile[getSrc(i)];
			int b = getImx();
			int sum = a+b;
			setFlags(s, isNeg(sum), isOvf(a, b, sum), (sum&65536) != 0, isZero(sum));
			s.secondary_regfile[getDst(i)] = sum&65535;
		};
		BiConsumer<Integer, State> addsx = (i, s) -> {
			int a = s.primary_regfile[getDst(i)];
			int b = getIms(i);
			int sum = a+b+1;
			setFlags(s, isNeg(sum), isOvf(a, b, sum), (sum&65536) != 0, isZero(sum));
			s.primary_regfile[getDst(i)] = sum&65535;
		};
		BiConsumer<Integer, State> addsy = (i, s) -> {
			int a = s.secondary_regfile[getDst(i)];
			int b = getIms(i);
			int sum = a+b+1;
			setFlags(s, isNeg(sum), isOvf(a, b, sum), (sum&65536) != 0, isZero(sum));
			s.secondary_regfile[getDst(i)] = sum&65535;
		};
		BiConsumer<Integer, State> addc = (i, s) -> {
			int a = s.primary_regfile[getDst(i)];
			int b = s.primary_regfile[getSrc(i)];
			int sum = a+b+(Main.isSet(s.reg_st, 13) ? 1 : 0);
			setFlags(s, isNeg(sum), isOvf(a, b, sum), (sum&65536) != 0, isZero(sum));
			s.primary_regfile[getDst(i)] = sum&65535;
		};
		BiConsumer<Integer, State> subrx = (i, s) -> {
			int a = s.primary_regfile[getDst(i)];
			int b = s.primary_regfile[getSrc(i)];
			int sum = a-b;
			setFlags(s, isNeg(sum), isOvf(a, b, sum), (sum&65536) != 0, isZero(sum));
			s.primary_regfile[getDst(i)] = sum&65535;
		};
		BiConsumer<Integer, State> subry = (i, s) -> {
			int a = s.secondary_regfile[getDst(i)];
			int b = s.primary_regfile[getSrc(i)];
			int sum = a-b;
			setFlags(s, isNeg(sum), isOvf(a, b, sum), (sum&65536) != 0, isZero(sum));
			s.secondary_regfile[getDst(i)] = sum&65535;
		};
		BiConsumer<Integer, State> subsx = (i, s) -> {
			int a = s.primary_regfile[getDst(i)];
			int b = getIms(i);
			int sum = a-b-1;
			setFlags(s, isNeg(sum), isOvf(a, b, sum), (sum&65536) != 0, isZero(sum));
			s.primary_regfile[getDst(i)] = sum&65535;
		};
		BiConsumer<Integer, State> subsy = (i, s) -> {
			int a = s.secondary_regfile[getDst(i)];
			int b = getIms(i);
			int sum = a-b-1;
			setFlags(s, isNeg(sum), isOvf(a, b, sum), (sum&65536) != 0, isZero(sum));
			s.secondary_regfile[getDst(i)] = sum&65535;
		};
		BiConsumer<Integer, State> subc = (i, s) -> {
			int a = s.primary_regfile[getDst(i)];
			int b = s.primary_regfile[getSrc(i)];
			int sum = a-b+(Main.isSet(s.reg_st, 13) ? 0 : -1);
			setFlags(s, isNeg(sum), isOvf(a, b, sum), (sum&65536) != 0, isZero(sum));
			s.primary_regfile[getDst(i)] = sum&65535;
		};
		BiConsumer<Integer, State> cmpx = (i, s) -> {
			int a = s.primary_regfile[getDst(i)];
			int b = s.primary_regfile[getSrc(i)];
			int sum = a-b;
			setFlags(s, isNeg(sum), isOvf(a, b, sum), (sum&65536) != 0, isZero(sum));
		};
		BiConsumer<Integer, State> cmpy = (i, s) -> {
			int a = s.secondary_regfile[getDst(i)];
			int b = s.secondary_regfile[getSrc(i)];
			int sum = a-b;
			setFlags(s, isNeg(sum), isOvf(a, b, sum), (sum&65536) != 0, isZero(sum));
		};
		BiConsumer<Integer, State> pen = (i, s) -> {
			final int[] rev = {0,8,4,12,2,10,6,14,1,9,5,13,3,11,7,15};
			int src = s.primary_regfile[getSrc(i)];
			int imm = getImx();
			int dst = 0;
			for(int cnt=0; cnt<4; cnt++) {
				int op = imm&15;
				imm >>= 4;
				int shift = (op&3)<<2;
				int nibble = (src & 15<<(shift)) >> shift;
				switch(op>>2) {
					case 1: nibble ^= 15; break;
					case 2: nibble = rev[nibble]; break;
					case 3: nibble = (op&1) != 0 ? 15 : 0; break;
				}
				nibble <<= 12;
				dst = dst>>4 | nibble;
			}
			setFlags(s, isNeg(dst), false, false, isZero(dst));
			s.primary_regfile[getDst(i)] = dst;
		};
		BiConsumer<Integer, State> peb = (i, s) -> {
			int imm = getImx();
			int dst_index = imm>>10&12, src_index = imm>>12&12;
			int src = s.primary_regfile[getSrc(i)], dst = src;
			src = src >> src_index & 15;
			int nibble = 0;
			for(int cnt=0; cnt<4; cnt++) {
				int op = imm&7;
				imm >>= 3;
				int shift = op&3;
				int bit = (src & 1<<shift) >> shift;
				bit ^= op>>2;
				bit <<= 3;
				nibble = nibble>>1 | bit;
			}
			dst &= ~(15<<dst_index);
			dst |= nibble<<dst_index;
			setFlags(s, isNeg(dst), false, false, isZero(dst));
			s.primary_regfile[getDst(i)] = dst;
		};
		
		BiConsumer<Integer, State> mulr = (i, s) -> {
			int a = s.primary_regfile[getDst(i)];
			int b = s.primary_regfile[getSrc(i)];
			int prod = a*b;
			setFlags(s, isNeg(prod), (prod>>>16) != 0, false, isZero(prod));
			s.primary_regfile[getDst(i)] = prod&65535;
		};
		BiConsumer<Integer, State> muli = (i, s) -> {
			int a = s.primary_regfile[getDst(i)];
			int b = getImx();
			int prod = a*b;
			setFlags(s, isNeg(prod), (prod>>>16) != 0, false, isZero(prod));
			s.primary_regfile[getDst(i)] = prod&65535;
		};
		BiConsumer<Integer, State> umlr = (i, s) -> {
			int a = s.primary_regfile[getDst(i)];
			int b = s.primary_regfile[getSrc(i)];
			int prod = (a*b)>>>16;
			setFlags(s, isNeg(prod), false, false, isZero(prod));
			s.primary_regfile[getDst(i)] = prod;
		};
		BiConsumer<Integer, State> umli = (i, s) -> {
			int a = s.primary_regfile[getDst(i)];
			int b = getImx();
			int prod = (a*b)>>>16;
			setFlags(s, isNeg(prod), false, false, isZero(prod));
			s.primary_regfile[getDst(i)] = prod;
		};
		BiConsumer<Integer, State> smlr = (i, s) -> {
			int a = s.primary_regfile[getDst(i)];
			int b = s.primary_regfile[getSrc(i)];
			a |= isNeg(a) ? 0xFFFF0000 : 0;
			b |= isNeg(b) ? 0xFFFF0000 : 0;
			int prod = (a*b)>>>16;
			setFlags(s, isNeg(prod), false, false, isZero(prod));
			s.primary_regfile[getDst(i)] = prod;
		};
		BiConsumer<Integer, State> smli = (i, s) -> {
			int a = s.primary_regfile[getDst(i)];
			int b = getImx();
			a |= isNeg(a) ? 0xFFFF0000 : 0;
			b |= isNeg(b) ? 0xFFFF0000 : 0;
			int prod = (a*b)>>>16;
			setFlags(s, isNeg(prod), false, false, isZero(prod));
			s.primary_regfile[getDst(i)] = prod;
		};
		BiConsumer<Integer, State> andr = (i, s) -> {
			int a = s.primary_regfile[getDst(i)];
			int b = s.primary_regfile[getSrc(i)];
			int res = a&b;
			setFlags(s, isNeg(res), false, false, isZero(res));
			s.primary_regfile[getDst(i)] = res&65535;
		};
		BiConsumer<Integer, State> andi = (i, s) -> {
			int a = s.primary_regfile[getDst(i)];
			int b = getImx();
			int res = a&b;
			setFlags(s, isNeg(res), false, false, isZero(res));
			s.primary_regfile[getDst(i)] = res&65535;
		};
		BiConsumer<Integer, State> nndr = (i, s) -> {
			int a = s.primary_regfile[getDst(i)];
			int b = s.primary_regfile[getSrc(i)];
			int res = ~(a&b);
			setFlags(s, isNeg(res), false, false, isZero(res));
			s.primary_regfile[getDst(i)] = res&65535;
		};
		BiConsumer<Integer, State> nndi = (i, s) -> {
			int a = s.primary_regfile[getDst(i)];
			int b = getImx();
			int res = ~(a&b);
			setFlags(s, isNeg(res), false, false, isZero(res));
			s.primary_regfile[getDst(i)] = res&65535;
		};
		BiConsumer<Integer, State> iorr = (i, s) -> {
			int a = s.primary_regfile[getDst(i)];
			int b = s.primary_regfile[getSrc(i)];
			int res = a|b;
			setFlags(s, isNeg(res), false, false, isZero(res));
			s.primary_regfile[getDst(i)] = res&65535;
		};
		BiConsumer<Integer, State> iori = (i, s) -> {
			int a = s.primary_regfile[getDst(i)];
			int b = getImx();
			int res = a|b;
			setFlags(s, isNeg(res), false, false, isZero(res));
			s.primary_regfile[getDst(i)] = res&65535;
		};
		BiConsumer<Integer, State> norr = (i, s) -> {
			int a = s.primary_regfile[getDst(i)];
			int b = s.primary_regfile[getSrc(i)];
			int res = ~(a|b);
			setFlags(s, isNeg(res), false, false, isZero(res));
			s.primary_regfile[getDst(i)] = res&65535;
		};
		BiConsumer<Integer, State> nori = (i, s) -> {
			int a = s.primary_regfile[getDst(i)];
			int b = getImx();
			int res = ~(a|b);
			setFlags(s, isNeg(res), false, false, isZero(res));
			s.primary_regfile[getDst(i)] = res&65535;
		};
		BiConsumer<Integer, State> xorr = (i, s) -> {
			int a = s.primary_regfile[getDst(i)];
			int b = s.primary_regfile[getSrc(i)];
			int res = a^b;
			setFlags(s, isNeg(res), false, false, isZero(res));
			s.primary_regfile[getDst(i)] = res&65535;
		};
		BiConsumer<Integer, State> xori = (i, s) -> {
			int a = s.primary_regfile[getDst(i)];
			int b = getImx();
			int res = a^b;
			setFlags(s, isNeg(res), false, false, isZero(res));
			s.primary_regfile[getDst(i)] = res&65535;
		};
		
		BiConsumer<Integer, State> bxtr = (i, s) -> setFlags(s, false, false, (s.primary_regfile[getDst(i)] & 1<<(s.primary_regfile[getSrc(i)]&15)) != 0, false);
		BiConsumer<Integer, State> bxts = (i, s) -> setFlags(s, false, false, (s.primary_regfile[getDst(i)] & 1<<getIms(i)) != 0, false);
		BiConsumer<Integer, State> bdpr = (i, s) -> {
			int val = s.primary_regfile[getDst(i)];
			int pos = s.primary_regfile[getSrc(i)]&15;
			val &= ~(1<<pos);
			val |= Main.isSet(s.reg_st, 13) ? (1<<pos) : 0;
			s.primary_regfile[getDst(i)] = val;
		};
		BiConsumer<Integer, State> bdps = (i, s) -> {
			int val = s.primary_regfile[getDst(i)];
			int pos = getIms(i);
			val &= ~(1<<pos);
			val |= Main.isSet(s.reg_st, 13) ? (1<<pos) : 0;
			s.primary_regfile[getDst(i)] = val;
		};
		BiConsumer<Integer, State> bngr = (i, s) -> s.primary_regfile[getDst(i)] ^= 1<<(s.primary_regfile[getSrc(i)]&15);
		BiConsumer<Integer, State> bngs = (i, s) -> s.primary_regfile[getDst(i)] ^= 1<<getIms(i);
		BiConsumer<Integer, State> rxtr = (i, s) -> setFlags(s, false, false, (s.reg_rf & 1<<(s.primary_regfile[getSrc(i)]&15)) != 0, false);
		BiConsumer<Integer, State> rxts = (i, s) -> setFlags(s, false, false, (s.reg_rf & 1<<getIms(i)) != 0, false);
		BiConsumer<Integer, State> rdpr = (i, s) -> {
			int val = s.reg_rf;
			int pos = s.primary_regfile[getSrc(i)]&15;
			val &= ~(1<<pos);
			val |= Main.isSet(s.reg_st, 13) ? (1<<pos) : 0;
			s.reg_rf = val;
		};
		BiConsumer<Integer, State> rdps = (i, s) -> {
			int val = s.reg_rf;
			int pos = getIms(i);
			val &= ~(1<<pos);
			val |= Main.isSet(s.reg_st, 13) ? (1<<pos) : 0;
			s.reg_rf = val;
		};
		BiConsumer<Integer, State> rbrr = (i, s) -> s.primary_regfile[getDst(i)] = (s.reg_rf & 1<<(s.primary_regfile[getSrc(i)]&15)) != 0 ? 0xFFFF : 0;
		BiConsumer<Integer, State> rbrs = (i, s) -> s.primary_regfile[getDst(i)] = (s.reg_rf & 1<<getIms(i)) != 0 ? 0xFFFF : 0;
		BiConsumer<Integer, State> asr = (i, s) -> {
			int val = (s.primary_regfile[getSrc(i)]<<16)>>16;
			int res = val>>1&65535;
			s.primary_regfile[getDst(i)] = res;
			setFlags(s, isNeg(res), false, (val&1) != 0, isZero(res));
		};
		BiConsumer<Integer, State> abrr = (i, s) -> {
			int val = (s.primary_regfile[getDst(i)]<<16)>>16;
			int res = val>>(s.primary_regfile[getSrc(i)]&15)&65535;
			s.primary_regfile[getDst(i)] = res;
			setFlags(s, isNeg(res), false, false, isZero(res));
		};
		BiConsumer<Integer, State> abrs = (i, s) -> {
			int val = (s.primary_regfile[getDst(i)]<<16)>>16;
			int res = val>>getIms(i)&65535;
			s.primary_regfile[getDst(i)] = res;
			setFlags(s, isNeg(res), false, false, isZero(res));
		};
		
		BiConsumer<Integer, State> lsr = (i, s) -> {
			int val = s.primary_regfile[getSrc(i)];
			int res = val>>1;
			s.primary_regfile[getDst(i)] = res;
			setFlags(s, isNeg(res), false, (val&1) != 0, isZero(res));
		};
		BiConsumer<Integer, State> lcr = (i, s) -> {
			int val = s.primary_regfile[getSrc(i)];
			int res = val>>1 | (Main.isSet(s.reg_st, 13) ? 1<<15 : 0);
			s.primary_regfile[getDst(i)] = res;
			setFlags(s, isNeg(res), false, (val&1) != 0, isZero(res));
		};
		BiConsumer<Integer, State> lbrr = (i, s) -> {
			int val = s.primary_regfile[getDst(i)];
			int res = val>>(s.primary_regfile[getSrc(i)]&15);
			s.primary_regfile[getDst(i)] = res;
			setFlags(s, isNeg(res), false, false, isZero(res));
		};
		BiConsumer<Integer, State> lbrs = (i, s) -> {
			int val = s.primary_regfile[getDst(i)];
			int res = val>>getIms(i);
			s.primary_regfile[getDst(i)] = res;
			setFlags(s, isNeg(res), false, false, isZero(res));
		};
		BiConsumer<Integer, State> lsl = (i, s) -> {
			int val = s.primary_regfile[getSrc(i)]<<1;
			s.primary_regfile[getDst(i)] = val&65535;
			setFlags(s, isNeg(val), false, (val&65536) != 0, isZero(val));
		};
		BiConsumer<Integer, State> lcl = (i, s) -> {
			int val = s.primary_regfile[getSrc(i)]<<1 | (Main.isSet(s.reg_st, 13) ? 1 : 0);
			s.primary_regfile[getDst(i)] = val&65535;
			setFlags(s, isNeg(val), false, (val&65536) != 0, isZero(val));
		};
		BiConsumer<Integer, State> lblr = (i, s) -> {
			int val = s.primary_regfile[getDst(i)]<<(s.primary_regfile[getSrc(i)]&15);
			s.primary_regfile[getDst(i)] = val&65535;
			setFlags(s, isNeg(val), false, false, isZero(val));
		};
		BiConsumer<Integer, State> lbls = (i, s) -> {
			int val = s.primary_regfile[getDst(i)]<<getIms(i);
			s.primary_regfile[getDst(i)] = val&65535;
			setFlags(s, isNeg(val), false, false, isZero(val));
		};
		BiConsumer<Integer, State> rbm = (i, s) -> {
			s.reg_rf &= ~(1<<getDst(i));
			s.reg_rf |= Main.isSet(s.reg_rf, getSrc(i)) ? 1<<getDst(i) : 0;
		};
		BiConsumer<Integer, State> rbn = (i, s) -> {
			s.reg_rf &= ~(1<<getDst(i));
			s.reg_rf |= Main.isSet(s.reg_rf, getSrc(i)) ? 0 : 1<<getDst(i);
		};
		BiConsumer<Integer, State> rbc = (i, s) -> s.reg_rf &= ~(Main.isSet(s.reg_rf, getSrc(i)) ? 0 : 1<<getDst(i));
		BiConsumer<Integer, State> rbd = (i, s) -> s.reg_rf |= Main.isSet(s.reg_rf, getSrc(i)) ? 1<<getDst(i) : 0;
		BiConsumer<Integer, State> ldrx = (i, s) -> s.primary_regfile[getDst(i)] = Main.getCPU().getMemory().read(s.primary_regfile[getSrc(i)]);
		BiConsumer<Integer, State> ldix = (i, s) -> s.primary_regfile[getDst(i)] = Main.getCPU().getMemory().read(s.primary_regfile[getSrc(i)] + getImx());
		BiConsumer<Integer, State> strx = (i, s) -> Main.getCPU().getMemory().write(s.primary_regfile[getSrc(i)], s.primary_regfile[getDst(i)]);
		BiConsumer<Integer, State> stix = (i, s) -> Main.getCPU().getMemory().write(s.primary_regfile[getSrc(i)] + getImx(), s.primary_regfile[getDst(i)]);
		
		BiConsumer<Integer, State> lsi = (i, s) -> s.primary_regfile[getDst(i)] = sxt8(getImh(i));
		
		BiConsumer<Integer, State> lui = (i, s) -> {
			int val = s.primary_regfile[getDst(i)] & 255;
			val |= getImh(i)<<8;
			s.primary_regfile[getDst(i)] = val;
		};
		
		BiConsumer<Integer, State> inp = (i, s) -> s.primary_regfile[getDst(i)] = Main.getCPU().getIO().read(getImh(i));
		
		BiConsumer<Integer, State> out = (i, s) -> Main.getCPU().getIO().write(getImh(i), s.primary_regfile[getDst(i)]);
		
		BiConsumer<Integer, State> brcr = (i, s) -> { if(evalCond(getOpc(i), s)) s.reg_ip = s.primary_regfile[getSrc(i)]; };
		BiConsumer<Integer, State> brpr = (i, s) -> { if(evalProp(getOpc(i), s.primary_regfile[getDst(i)], s)) s.reg_ip = s.primary_regfile[getSrc(i)]; };
		
		BiConsumer<Integer, State> brci = (i, s) -> { if(evalCond(getOpc(i), s)) s.reg_ip = s.primary_regfile[getSrc(i)] + getImx(); };
		BiConsumer<Integer, State> brpi = (i, s) -> { if(evalProp(getOpc(i), s.primary_regfile[getDst(i)], s)) s.reg_ip = s.primary_regfile[getSrc(i)] + getImx(); };
		
		BiConsumer<Integer, State> ldry = (i, s) -> s.primary_regfile[getDst(i)] = Main.getCPU().getMemory().read(s.secondary_regfile[getSrc(i)]);
		BiConsumer<Integer, State> mldry = (i, s) -> s.primary_regfile[getDst(i)] = Main.getCPU().getMemory().read(--s.secondary_regfile[getSrc(i)]);
		BiConsumer<Integer, State> ldryp = (i, s) -> s.primary_regfile[getDst(i)] = Main.getCPU().getMemory().read(s.secondary_regfile[getSrc(i)]++);
		BiConsumer<Integer, State> pldry = (i, s) -> s.primary_regfile[getDst(i)] = Main.getCPU().getMemory().read(++s.secondary_regfile[getSrc(i)]);
		BiConsumer<Integer, State> ldiy = (i, s) -> s.primary_regfile[getDst(i)] = Main.getCPU().getMemory().read(s.secondary_regfile[getSrc(i)] + getImx());
		BiConsumer<Integer, State> mldiy = (i, s) -> s.primary_regfile[getDst(i)] = Main.getCPU().getMemory().read(--s.secondary_regfile[getSrc(i)] + getImx());
		BiConsumer<Integer, State> ldiyp = (i, s) -> s.primary_regfile[getDst(i)] = Main.getCPU().getMemory().read(s.secondary_regfile[getSrc(i)]++ + getImx());
		BiConsumer<Integer, State> pldiy = (i, s) -> s.primary_regfile[getDst(i)] = Main.getCPU().getMemory().read(++s.secondary_regfile[getSrc(i)] + getImx());
		BiConsumer<Integer, State> stry = (i, s) -> Main.getCPU().getMemory().write(s.secondary_regfile[getSrc(i)], s.primary_regfile[getDst(i)]);
		BiConsumer<Integer, State> mstry = (i, s) -> Main.getCPU().getMemory().write(--s.secondary_regfile[getSrc(i)], s.primary_regfile[getDst(i)]);
		BiConsumer<Integer, State> stryp = (i, s) -> Main.getCPU().getMemory().write(s.secondary_regfile[getSrc(i)]++, s.primary_regfile[getDst(i)]);
		BiConsumer<Integer, State> pstry = (i, s) -> Main.getCPU().getMemory().write(++s.secondary_regfile[getSrc(i)], s.primary_regfile[getDst(i)]);
		BiConsumer<Integer, State> stiy = (i, s) -> Main.getCPU().getMemory().write((s.secondary_regfile[getSrc(i)]) + getImx(), s.primary_regfile[getDst(i)]);
		BiConsumer<Integer, State> mstiy = (i, s) -> Main.getCPU().getMemory().write((--s.secondary_regfile[getSrc(i)]) + getImx(), s.primary_regfile[getDst(i)]);
		BiConsumer<Integer, State> stiyp = (i, s) -> Main.getCPU().getMemory().write((s.secondary_regfile[getSrc(i)]++) + getImx(), s.primary_regfile[getDst(i)]);
		BiConsumer<Integer, State> pstiy = (i, s) -> Main.getCPU().getMemory().write((++s.secondary_regfile[getSrc(i)]) + getImx(), s.primary_regfile[getDst(i)]);
		
		BiConsumer<Integer, State> brco = (i, s) -> { if(evalCond(getOpc(i), s)) s.reg_ip += sxt8(getIml(i))-1; };
		
		/* BEGIN IMPLEMENTATION PLACEMENT */
		impls.add(0, sig);
		impls.add(1, movxx);
		impls.add(2, movyx);
		impls.add(3, movxy);
		impls.add(4, movyy);
		impls.add(5, lst);
		impls.add(6, sst);
		impls.add(7, lrf );
		impls.add(8, srf);
		impls.add(9, ljp);
		impls.add(10, sjp);
		impls.add(11, lip);
		impls.add(12, sip);
		impls.add(13, jmpo);
		impls.add(14, jnl);
		impls.add(15, prdr);
		
		for(int i=16; i<24; i++) impls.add(i, prdc);
		for(int i=24; i<32; i++) impls.add(i, prdp);
		
		for(int i=32; i<40; i++) impls.add(i, rbcc);
		for(int i=40; i<48; i++) impls.add(i, rbcp);
		
		for(int i=48; i<56; i++) impls.add(i, rbdc);
		for(int i=56; i<64; i++) impls.add(i, rbdp);
		
		impls.add(64, addrx);
		impls.add(65, addry);
		impls.add(66, addix);
		impls.add(67, addiy);
		impls.add(68, addsx);
		impls.add(69, addsy);
		impls.add(70, addc);
		impls.add(71, subrx);
		impls.add(72, subry);
		impls.add(73, subsx);
		impls.add(74, subsy);
		impls.add(75, subc);
		impls.add(76, cmpx);
		impls.add(77, cmpy);
		impls.add(78, pen);
		impls.add(79, peb);
		
		impls.add(80, mulr);
		impls.add(81, muli);
		impls.add(82, umlr);
		impls.add(83, umli);
		impls.add(84, smlr);
		impls.add(85, smli);
		impls.add(86, andr);
		impls.add(87, andi);
		impls.add(88, nndr);
		impls.add(89, nndi);
		impls.add(90, iorr);
		impls.add(91, iori);
		impls.add(92, norr);
		impls.add(93, nori);
		impls.add(94, xorr);
		impls.add(95, xori);
		
		impls.add(96, bxtr);
		impls.add(97, bxts);
		impls.add(98, bdpr);
		impls.add(99, bdps);
		impls.add(100, bngr);
		impls.add(101, bngs);
		impls.add(102, rxtr);
		impls.add(103, rxts);
		impls.add(104, rdpr);
		impls.add(105, rdps);
		impls.add(106, rbrr);
		impls.add(107, rbrs);
		impls.add(108, asr);
		impls.add(109, invalid);
		impls.add(110, abrr);
		impls.add(111, abrs);

		impls.add(112, lsr);
		impls.add(113, lcr);
		impls.add(114, lbrr);
		impls.add(115, lbrs);
		impls.add(116, lsl);
		impls.add(117, lcl);
		impls.add(118, lblr);
		impls.add(119, lbls);
		impls.add(120, rbm);
		impls.add(121, rbn);
		impls.add(122, rbc);
		impls.add(123, rbd);
		impls.add(124, ldrx);
		impls.add(125, ldix);
		impls.add(126, strx);
		impls.add(127, stix);
		
		for(int i=128; i<144; i++) impls.add(i, lsi);
		
		for(int i=144; i<160; i++) impls.add(i, lui);
		
		for(int i=160; i<176; i++) impls.add(i, inp);
		
		for(int i=176; i<192; i++) impls.add(i, out);
		
		for(int i=192; i<200; i++) impls.add(i, brcr);
		for(int i=200; i<208; i++) impls.add(i, brpr);
		
		for(int i=208; i<216; i++) impls.add(i, brci);
		for(int i=216; i<224; i++) impls.add(i, brpi);
		
		impls.add(224, ldry);
		impls.add(225, mldry);
		impls.add(226, ldryp);
		impls.add(227, pldry);
		impls.add(228, ldiy);
		impls.add(229, mldiy);
		impls.add(230, ldiyp);
		impls.add(231, pldiy);
		impls.add(232, stry);
		impls.add(233, mstry);
		impls.add(234, stryp);
		impls.add(235, pstry);
		impls.add(236, stiy);
		impls.add(237, mstiy);
		impls.add(238, stiyp);
		impls.add(239, pstiy);
		
		for(int i=240; i<248; i++) impls.add(i, brco);
		for(int i=248; i<256; i++) impls.add(i, invalid);
	}
	
	// Instruction field spliting
	private static int getOpc(int instr_word) { return instr_word>>8&255; }
	private static int getSrc(int instr_word) { return instr_word>>4&15; }
	private static int getDst(int instr_word) { return instr_word&15; }
	private static int getImh(int instr_word) { return instr_word>>4&255; }
	private static int getIml(int instr_word) { return instr_word&255; }
	private static int getIms(int instr_word) { return instr_word>>4&15; }
	
	// Sign-extend 8 -> 16
	private static int sxt8(int value) {
		if(value <= 127) return value;
		else return 0xFF00 | value;
	}
	
	// Read next word after instruction (contains 16 bit immediate)
	private static int getImx() {
		CPU cpu = Main.getCPU();
		return cpu.getMemory().read(cpu.getState().reg_ip++);
	}
	
	// Evaluate a propery
	private static boolean evalProp(int opcode, int regval, State state) {
		switch(opcode&7) {
			case 0: return regval == 0;
			case 1: return regval == state.reg_rf;
			case 2: return (regval&32768) != 0;
			case 3: return (regval&1) != 0;
			case 4: return regval != 0;
			case 5: return regval != state.reg_rf;
			case 6: return (regval&32768) == 0;
			case 7: return (regval&1) == 0;
			default: return false;
		}
	}
	
	// Evaluate a condition
	private static boolean evalCond(int opcode, State state) {
		switch(opcode&7) {
			case 0: return Main.isSet(state.reg_st, 13);
			case 1: return Main.isSet(state.reg_st, 14);
			case 2: return Main.isSet(state.reg_st, 12);
			case 3: return !Main.isSet(state.reg_st, 12);
			case 4: return !Main.isSet(state.reg_st, 13);
			case 5: return !Main.isSet(state.reg_st, 13) || Main.isSet(state.reg_st, 12);
			case 6: return Main.isSet(state.reg_st, 15) != Main.isSet(state.reg_st, 14);
			case 7: return (Main.isSet(state.reg_st, 15) != Main.isSet(state.reg_st, 14)) || Main.isSet(state.reg_st, 12);
			default: return false;
		}
	}
	
	// Check if a 16 bit value is zero
	private static boolean isZero(int value) {
		return (value&65535) == 0;
	}
	
	// Check if a 16 bit value is negative
	private static boolean isNeg(int value) {
		return (value&32768) != 0;
	}
	
	// Calculate the overflow condition based on the sum of, and a and b
	private static boolean isOvf(int a, int b, int sum) {
		return (isNeg(a) != isNeg(sum)) && (isNeg(a) == isNeg(b));
	}
	
	// Modify just the top 4 bits of the status register containing the alu flags
	private static void setFlags(State s, boolean n, boolean v, boolean c, boolean z) {
		int flags = s.reg_st & 4095;
		flags |= z ? 1<<12 : 0;
		flags |= c ? 1<<13 : 0;
		flags |= v ? 1<<14 : 0;
		flags |= n ? 1<<15 : 0;
		s.reg_st = flags;
	}
}
