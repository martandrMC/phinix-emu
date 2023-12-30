#bits 16

#subruledef hex {
	0 => 0x0`4
	1 => 0x1`4
	2 => 0x2`4
	3 => 0x3`4
	4 => 0x4`4
	5 => 0x5`4
	6 => 0x6`4
	7 => 0x7`4
	8 => 0x8`4
	9 => 0x9`4
	A => 0xA`4
	B => 0xB`4
	C => 0xC`4
	D => 0xD`4
	E => 0xE`4
	F => 0xF`4
}

#subruledef prim {
	x{x: hex} => x
	
	zr => 0x0`4
	t0 => 0x1`4
	at => 0x1`4
	t1 => 0x2`4
	t2 => 0x3`4
	t3 => 0x4`4
	t4 => 0x5`4
	a0 => 0x6`4
	rp => 0x6`4
	a1 => 0x7`4
	a2 => 0x8`4
	a3 => 0x9`4
	s0 => 0xA`4
	fp => 0xA`4
	s1 => 0xB`4
	s2 => 0xC`4
	s3 => 0xD`4
	s4 => 0xE`4
	s5 => 0xF`4
}

#subruledef secd {
	y{y: hex} => y
	
	a4 => 0x0`4
	a5 => 0x1`4
	a6 => 0x2`4
	a7 => 0x3`4
	t5 => 0x4`4
	t6 => 0x5`4
	t7 => 0x6`4
	t8 => 0x7`4
	s6 => 0x8`4
	s7 => 0x9`4
	gp => 0xA`4
	sp => 0xB`4
	k0 => 0xC`4
	k1 => 0xD`4
	k2 => 0xE`4
	kp => 0xF`4
}

#subruledef cond {
	cr => 0x0`3
	ae => 0x0`3
	ov => 0x1`3
	ze => 0x2`3
	eq => 0x2`3
	nz => 0x3`3
	nq => 0x3`3
	nc => 0x4`3
	bl => 0x4`3
	be => 0x5`3
	lt => 0x6`3
	le => 0x7`3
}

#subruledef prop {
	zer => 0x0`3
	ref => 0x1`3
	neg => 0x2`3
	odd => 0x3`3
	nzr => 0x4`3
	nrf => 0x5`3
	pos => 0x6`3
	evn => 0x7`3
}

#ruledef native {
	sig {ims: u4}								=> 0x00`8	@ ims	@ 0x0`4
	mov {dst: prim} {src: prim}					=> 0x01`8	@ src	@ dst
	mov {dst: secd} {src: prim}					=> 0x02`8	@ src	@ dst
	mov {dst: prim} {src: secd}					=> 0x03`8	@ src	@ dst
	mov {dst: secd} {src: secd}					=> 0x04`8	@ src	@ dst
	mov st {src: prim}							=> 0x05`8	@ src	@ 0x0`4
	mov {dst: prim} st							=> 0x06`8	@ 0x0`4	@ dst
	mov rf {src: prim}							=> 0x07`8	@ src	@ 0x0`4
	mov {dst: prim} rf							=> 0x08`8	@ 0x0`4	@ dst
	mov jp {src: prim}							=> 0x09`8	@ src	@ 0x0`4
	mov {dst: prim} jp							=> 0x0A`8	@ 0x0`4	@ dst
	jmp jp										=> 0x0B`8	@ 0x04	@ 0x0`4
	sip {dst: prim} {ims: u4}					=> 0x0C`8	@ ims	@ dst
	jmp ip {iml: i16}							=> {
		val = iml - $
		0x0D`8	@ val`8
	}
	jnl {dst: prim} {src: prim} {imx: u16}		=> 0x0E`8	@ src	@ dst	@ imx
	prd rf.{ims: hex}							=> 0x0F`8	@ ims	@ 0x0`4
	prd {cnd: cond}								=> 0x02`5	@ cnd	@ 0x0`4	@ 0x0`4
	prd {dst: prim}.{prp: prop}					=> 0x03`5	@ prp	@ 0x0`4	@ dst
	rbc rf.{ims: hex} {cnd: cond}				=> 0x04`5	@ cnd	@ ims	@ 0x0`4
	rbc rf.{ims: hex} {dst:prim}.{prp: prop}	=> 0x05`5	@ prp	@ ims	@ dst
	rbd rf.{ims: hex} {cnd: cond}				=> 0x06`5	@ cnd	@ ims	@ 0x0`4
	rbd rf.{ims: hex} {dst:prim}.{prp: prop}	=> 0x07`5	@ prp	@ ims	@ dst

	add {dst: prim} {src: prim}				=> 0x40`8	@ src	@ dst
	add {dst: secd} {src: prim}				=> 0x41`8	@ src	@ dst
	add {dst: prim} {src: prim} {imx: i16}	=> 0x42`8	@ src	@ dst	@ imx
	add {dst: secd} {src: prim} {imx: i16}	=> 0x43`8	@ src	@ dst	@ imx
	ads {dst: prim} {ims: u4}				=> 0x44`8	@ ims	@ dst
	ads {dst: secd} {ims: u4}				=> 0x45`8	@ ims	@ dst
	adc {dst: prim} {src: prim}				=> 0x46`8	@ src	@ dst
	sub {dst: prim} {src: prim}				=> 0x47`8	@ src	@ dst
	sub {dst: secd} {src: prim}				=> 0x48`8	@ src	@ dst
	sbs {dst: prim} {ims: u4}				=> 0x49`8	@ ims	@ dst
	sbs {dst: secd} {ims: u4}				=> 0x4A`8	@ ims	@ dst
	sbc {dst: prim} {src: prim}				=> 0x4B`8	@ src	@ dst
	cmp {dst: prim} {src: prim}				=> 0x4C`8	@ src	@ dst
	cmp {dst: secd} {src: secd}				=> 0x4D`8	@ src	@ dst
	pen {dst: prim} {src: prim} {imx: i16}	=> 0x4E`8	@ src	@ dst	@ imx
	peb {dst: prim} {src: prim} {imx: i16}	=> 0x4F`8	@ src	@ dst	@ imx
	mul {dst: prim} {src: prim}				=> 0x50`8	@ src	@ dst
	mul {dst: prim} {src: prim} {imx: i16}	=> 0x51`8	@ src	@ dst	@ imx
	uml {dst: prim} {src: prim}				=> 0x52`8	@ src	@ dst
	uml {dst: prim} {src: prim} {imx: i16}	=> 0x53`8	@ src	@ dst	@ imx
	sml {dst: prim} {src: prim}				=> 0x54`8	@ src	@ dst
	sml {dst: prim} {src: prim} {imx: i16}	=> 0x55`8	@ src	@ dst	@ imx
	and {dst: prim} {src: prim}				=> 0x56`8	@ src	@ dst
	and {dst: prim} {src: prim} {imx: i16}	=> 0x57`8	@ src	@ dst	@ imx
	nnd {dst: prim} {src: prim}				=> 0x58`8	@ src	@ dst
	nnd {dst: prim} {src: prim} {imx: i16}	=> 0x59`8	@ src	@ dst	@ imx
	ior {dst: prim} {src: prim}				=> 0x5A`8	@ src	@ dst
	ior {dst: prim} {src: prim} {imx: i16}	=> 0x5B`8	@ src	@ dst	@ imx
	nor {dst: prim} {src: prim}				=> 0x5C`8	@ src	@ dst
	nor {dst: prim} {src: prim} {imx: i16}	=> 0x5D`8	@ src	@ dst	@ imx
	xor {dst: prim} {src: prim}				=> 0x5E`8	@ src	@ dst
	xor {dst: prim} {src: prim} {imx: i16}	=> 0x5F`8	@ src	@ dst	@ imx
	bxt {dst: prim}.{src: prim}				=> 0x60`8	@ src	@ dst
	bxt {dst: prim}.{ims: hex}				=> 0x61`8	@ ims	@ dst
	bdp {dst: prim}.{src: prim}				=> 0x62`8	@ src	@ dst
	bdp {dst: prim}.{ims: hex}				=> 0x63`8	@ ims	@ dst
	bng {dst: prim}.{src: prim}				=> 0x64`8	@ src	@ dst
	bng {dst: prim}.{ims: hex}				=> 0x65`8	@ ims	@ dst
	bxt rf.{src: prim}						=> 0x66`8	@ src	@ 0x0`4
	bxt rf.{ims: hex}						=> 0x67`8	@ ims	@ 0x0`4
	bdp rf.{src: prim}						=> 0x68`8	@ src	@ 0x0`4
	bdp rf.{ims: hex}						=> 0x69`8	@ ims	@ 0x0`4
	rbr rf.{src: prim}						=> 0x6A`8	@ src	@ 0x0`4
	rbr rf.{ims: hex}						=> 0x6B`8	@ ims	@ 0x0`4
	asr {dst: prim} {src: prim}				=> 0x6C`8	@ src	@ dst
	; blank space opcode hex 6D
	abr {dst: prim} {src: prim}				=> 0x6E`8	@ src	@ dst
	abr {dst: prim} {ims: u4}				=> 0x6F`8	@ ims	@ dst
	lsr {dst: prim} {src: prim}				=> 0x70`8	@ src	@ dst
	lcr {dst: prim} {src: prim}				=> 0x71`8	@ src	@ dst
	lbr {dst: prim} {src: prim}				=> 0x72`8	@ src	@ dst
	lbr {dst: prim} {ims: u4}				=> 0x73`8	@ ims	@ dst
	lsl {dst: prim} {src: prim}				=> 0x74`8	@ src	@ dst
	lcl {dst: prim} {src: prim}				=> 0x75`8	@ src	@ dst
	lbl {dst: prim} {src: prim}				=> 0x76`8	@ src	@ dst
	lbl {dst: prim} {ims: u4}				=> 0x77`8	@ ims	@ dst
	rbm	rf.{dst: hex} rf.{src: hex}			=> 0x78`8	@ src	@ dst
	rbn	rf.{dst: hex} rf.{src: hex}			=> 0x79`8	@ src	@ dst
	rbc	rf.{dst: hex} rf.{src: hex}			=> 0x7A`8	@ src	@ dst
	rbd	rf.{dst: hex} rf.{src: hex}			=> 0x7B`8	@ src	@ dst
	mld {dst: prim} {src: prim}				=> 0x7C`8	@ src	@ dst
	mld {dst: prim} {src: prim} {imx: i16}	=> 0x7D`8	@ src	@ dst	@ imx
	mst {dst: prim} {src: prim}				=> 0x7E`8	@ src	@ dst
	mst {dst: prim} {src: prim}	{imx: i16}	=> 0x7F`8	@ src	@ dst	@ imx

	lsi {dst: prim} {iml: i8}	=> 0x8`4	@ iml	@ dst
	lui {dst: prim} {iml: u8}	=> 0x9`4	@ iml	@ dst
	inp {dst: prim} {iml: u8}	=> 0xA`4	@ iml	@ dst
	out {dst: prim} {iml: u8}	=> 0xB`4	@ iml	@ dst

	jmp {src: prim} {cnd: cond}							=> 0x18`5	@ cnd	@ src	@ 0x0`4
	jmp {src: prim} {dst: prim}.{prp: prop}				=> 0x19`5	@ prp	@ src	@ dst
	jmp {src: prim} {imx: i16} {cnd: cond}				=> 0x1A`5	@ cnd	@ src	@ 0x0`4	@ imx
	jmp {src: prim} {imx: i16} {dst: prim}.{prp: prop}	=> 0x1B`5	@ prp	@ src	@ dst	@ imx
	mld {dst: prim} {src: secd}							=> 0xE0`8	@ src	@ dst
	mld {dst: prim} -{src: secd}						=> 0xE1`8	@ src	@ dst
	mld {dst: prim} {src: secd}+						=> 0xE2`8	@ src	@ dst
	mld {dst: prim} +{src: secd}						=> 0xE3`8	@ src	@ dst
	mld {dst: prim} {src: secd} {imx: i16}				=> 0xE4`8	@ src	@ dst	@ imx
	mld {dst: prim} -{src: secd} {imx: i16}				=> 0xE5`8	@ src	@ dst	@ imx
	mld {dst: prim} {src: secd}+ {imx: i16}				=> 0xE6`8	@ src	@ dst	@ imx
	mld {dst: prim} +{src: secd} {imx: i16}				=> 0xE7`8	@ src	@ dst	@ imx
	mst {dst: prim} {src: secd}							=> 0xE8`8	@ src	@ dst
	mst {dst: prim} -{src: secd}						=> 0xE9`8	@ src	@ dst
	mst {dst: prim} {src: secd}+						=> 0xEA`8	@ src	@ dst
	mst {dst: prim} +{src: secd}						=> 0xEB`8	@ src	@ dst
	mst {dst: prim} {src: secd} {imx: i16}				=> 0xEC`8	@ src	@ dst	@ imx
	mst {dst: prim} -{src: secd} {imx: i16}				=> 0xED`8	@ src	@ dst	@ imx
	mst {dst: prim} {src: secd}+ {imx: i16}				=> 0xEE`8	@ src	@ dst	@ imx
	mst {dst: prim} +{src: secd} {imx: i16}				=> 0xEF`8	@ src	@ dst	@ imx
	jmp	ip {iml: i16} {cnd: cond}						=> {
		val = iml - $
		0x1E`5	@ cnd	@ val`8
	}
	; blank space opcode hex F8
	; blank space opcode hex F9
	; blank space opcode hex FA
	; blank space opcode hex FB
	; blank space opcode hex FC
	; blank space opcode hex FD
	; blank space opcode hex FE
	; blank space opcode hex FF
}

; Implementations with the same mnemonic as previous ones can not use them in an asm block
; You will need to spell out the binary expansion again, which will definetly not cause
; a problem in the future...
#ruledef pseudo {
	hlt => asm { sig 0 }
	nop => asm { mov zr zr }

	nul {dst: prim} => asm { mov {dst} zr }
	nul {dst: secd}	=> asm { mov {dst} zr }
	nul rf			=> asm { mov rf zr }
	nul st			=> asm { bxt zr.0 }

	inc {dst: prim}	=> asm { ads {dst} 0 }
	inc {dst: secd}	=> asm { ads {dst} 0 }
	dec {dst: prim}	=> asm { sbs {dst} 0 }
	dec {dst: secd}	=> asm { sbs {dst} 0 }

	lfi {dst: prim} {imx: i16}	=> 0x42`8	@ 0x0`4	@ dst	@ imx
	lfi {dst: secd} {imx: i16}	=> 0x43`8	@ 0x0`4	@ dst	@ imx
	bit {src: prim} {imx: i16}	=> 0x57`8	@ src	@ 0x0`4	@ imx

	sub {dst: prim} {imx: i16}				=> {
		val = -imx
		0x42`8	@ dst	@ dst	@ val`16
	}
	sub {dst: secd} {imx: i16}				=> {
		val = -imx
		0x43`8	@ dst	@ dst	@ val`16
	}
	sub {dst: prim} {src: prim} {imx: i16}	=> {
		val = -imx
		0x42`8	@ src	@ dst	@ val`16
	}
	sub {dst: secd} {src: prim} {imx: i16}	=> {
		val = -imx
		0x43`8	@ src	@ dst	@ val`16
	}
	cmp {src: prim} {imx: i16}				=> {
		val = -imx
		0x42`8	@ src	@ 0x0`4	@ val`16
	}
	cmp {src: secd} {imx: i16}				=> {
		val = -imx
		0x43`8	@ src	@ 0x0`4	@ val`16
	}

	add {dst: prim} {imx: i16}	=> 0x42`8	@ dst	@ dst	@ imx
	add {dst: secd} {imx: i16}	=> 0x43`8	@ dst	@ dst	@ imx
	pen {dst: prim} {imx: i16}	=> 0x4E`8	@ dst	@ dst	@ imx
	peb {dst: prim} {imx: i16}	=> 0x4F`8	@ dst	@ dst	@ imx
	mul {dst: prim} {imx: i16}	=> 0x51`8	@ dst	@ dst	@ imx
	uml {dst: prim} {imx: i16}	=> 0x53`8	@ dst	@ dst	@ imx
	sml {dst: prim} {imx: i16}	=> 0x55`8	@ dst	@ dst	@ imx
	and {dst: prim} {imx: i16}	=> 0x57`8	@ dst	@ dst	@ imx
	nnd {dst: prim} {imx: i16}	=> 0x59`8	@ dst	@ dst	@ imx
	ior {dst: prim} {imx: i16}	=> 0x5B`8	@ dst	@ dst	@ imx
	nor {dst: prim} {imx: i16}	=> 0x5D`8	@ dst	@ dst	@ imx
	xor {dst: prim} {imx: i16}	=> 0x5F`8	@ dst	@ dst	@ imx

	jnl {dst: prim} {src: prim}	=> 0x0E`8	@ src	@ dst	@ 0x0`16
	jnl {dst: prim} {imx: u16}	=> 0x0E`8	@ 0x0`4	@ dst	@ imx
	jnl {src: prim} {imx: u16}	=> 0x0E`8	@ src	@ 0x6`4	@ imx
	jnl {src: prim}				=> 0x0E`8	@ src	@ 0x6`4	@ 0x0`16
	jnl {imx: u16}				=> 0x0E`8	@ 0x0`4	@ 0x6`4	@ imx

	jmp {src: prim} 						=> 0x19`5	@ 0x0`3	@ src	@ 0x0`4
	jmp {imx: i16} {cnd: cond}				=> 0x1A`5	@ cnd	@ 0x0`4	@ 0x0`4	@ imx
	jmp {imx: i16} 							=> 0x1B`5	@ 0x0`3	@ 0x0`4	@ 0x0`4	@ imx
	jmp {src: prim} {imx: i16}				=> 0x1B`5	@ 0x0`3	@ src	@ 0x0`4	@ imx
	jmp {imx: i16} {dst: prim}.{prp: prop}	=> 0x1B`5	@ prp	@ 0x0`4	@ dst	@ imx

	mld {dst: prim} {imx: i16}	=> 0x7D`8	@ 0x0`4	@ dst	@ imx
	mst {dst: prim} {imx: i16}	=> 0x7F`8	@ 0x0`4	@ dst	@ imx
	pop {dst: prim}				=> 0xE2`8	@ 0xB`4	@ dst
	psh {dst: prim}				=> 0xE9`8	@ 0xB`4	@ dst
}

; Soon :tm:
#ruledef macro {

}