;__________________________________________________

cmd_read:
psh rp
psh fp
mov fp sp
	lfi a4 read_text
	jnl print_packed_string
	mld a1 s7+
	jnl print_hex_word
	lfi t0 0x0D0A
	out t0 ttypkd
mov sp fp
pop fp
pop rp
jmp rp

cmd_write:
psh rp
psh fp
mov fp sp
	jnl expect_parameter
	jmp .exit a1.nzr
	lfi a4 invalid_text
	jnl read_hex_word
	prd a2.nzr
	jnl print_packed_string
	prd a2.zer
	mst a1 s7+
	.exit:
mov sp fp
pop fp
pop rp
jmp rp

cmd_dump:
psh rp
psh fp
mov fp sp
	jnl expect_parameter
	jmp .exit a1.nzr
	lfi a4 invalid_text
	jnl read_hex_word
	prd a2.nzr
	jnl print_packed_string
	prd a2.zer

	mov s1 a1
	.loop:
	jmp .exit s1.zer
		lsi t0 0x09
		out t0 ttyraw

		mov a1 s7
		lbr a1 3
		lbl a1 3
		mov s7 a1
		jnl print_hex_word

		lfi t0 0x3A20
		out t0 ttypkd
		lfi t0 0x2020
		out t0 ttypkd

		jnl dump_line_hex
		lfi t0 0x2020
		out t0 ttypkd
		jnl dump_line_text
		lfi t0 0x0D0A
		out t0 ttypkd
		ads s7 7
	sbs s1 7
	jmp ip .loop cr
	.exit:
mov sp fp
pop fp
pop rp
jmp rp

cmd_load:
psh rp
psh fp
mov fp sp
	jnl empty_input
	inp t0 ttypkd
	cmp t0 0x702B
	jmp ip .skip eq
	lfi a4 invalid_text
	jnl print_packed_string
	jmp ip .exit
	.skip:

	lsi t0 2
	mst t0 load_state
	jnl load_convert_twochar
	jmp .exit a2.nzr
	mst a1 load_buffer

	jnl load_get_word
	mov s3 a1
	jnl load_get_word
	mov s1 a1
	jnl load_get_word
	mov s2 a1

	mld t0 load_state
	jmp .exit t0.neg
	mov s7 s3
	lsi t0 0x09
	out t0 ttyraw
	mov a1 s1
	jnl print_hex_word
	lfi a4 load_text
	jnl print_packed_string
	.loop:
		jnl load_get_word
		mst a1 s7+
		dec s1
		mld t0 load_state
		jmp .exit t0.neg
	jmp .loop s1.nzr
	.exit:
	prd s2.nzr
	jnl s2
mov sp fp
pop fp
pop rp
jmp rp

cmd_addr:
psh rp
psh fp
mov fp sp
	jnl expect_parameter
	jmp .exit a1.nzr
	lfi a4 invalid_text
	jnl read_hex_word
	prd a2.nzr
	jnl print_packed_string
	prd a2.zer
	mov s7 a1
	.exit:
mov sp fp
pop fp
pop rp
jmp rp

cmd_jump:
	mov t0 s7
	jmp t0

cmd_help:
	lfi a4 help_text
	jmp print_packed_string

cmd_clear:
	lfi a4 clear_text
	jmp print_packed_string

cmd_quit:
	lfi a4 quit_text
	jnl print_packed_string
	hlt

;__________________________________________________

; Reads a character expecting a whitespace
; which signifies the presence of a parameter
; RETURNS: a1
; CLOBERS: t0 a4
expect_parameter:
psh rp
psh fp
mov fp sp
	nul a1
	inp t0 ttyraw
	cmp t0 0x0020
	jmp ip .exit eq
	cmp t0 0x0009
	jmp ip .exit eq
	lfi a4 invalid_text
	jnl print_packed_string
	lsi a1 1
	.exit:
mov sp fp
pop fp
pop rp
jmp rp

; Prints out 8 hex words separated by spaces
; starting at the current address (s7)
; CLOBERS: t0 a1
dump_line_hex:
psh rp
psh fp
mov fp sp
	psh s1
	lsi s1 8
	.loop:
		mld a1 s7+
		jnl print_hex_word
		lsi t0 0x20
		out t0 ttyraw
	dec s1
	jmp .loop s1.nzr
	pop s1
	sbs s7 7
mov sp fp
pop fp
pop rp
jmp rp

; Prints out 8 twochars starting
; at the current address (s7)
; CLOBERS: t0 t1 rf.0
dump_line_text:
	lsi t0 8
	.loop:
		mld t1 s7
		lbr t1 8
		rbd rf.0 zr.zer
		cmp t1 0x0020
		rbc rf.0 ae
		cmp t1 0x007F
		rbc rf.0 bl
		bxt rf.0
		prd nc
		lsi t1 0x2E
		out t1 ttyraw

		mld t1 s7+
		and t1 0x00FF
		rbd rf.0 zr.zer
		cmp t1 0x0020
		rbc rf.0 ae
		cmp t1 0x007F
		rbc rf.0 bl
		bxt rf.0
		prd nc
		lsi t1 0x2E
		out t1 ttyraw
	dec t0
	jmp .loop t0.nzr
	sbs s7 7
jmp rp

; Reads a base64 decoded twochar
; and returns the 12 bits in a1
; a2 will be non-zero if the end is met
; RETURNS: a1 a2
; CLOBERS: t0 
load_convert_twochar:
	nul a2
	inp t0 ttypkd
	cmp t0 0x2121
	prd eq
	lsi a2 1
	sub t0 0x3030
	bit t0 0xC0C0
	prd nz
	nul t0

	lbl t0 2
	and a1 t0 0xFF00
	and t0 0x00FF
	lbl t0 2
	ior a1 t0
jmp rp

; Compresses the 12 bits returned from load_convert_twochar
; into 16 bit words. Returns the new one in a1.
; RETURNS: a1
; CLOBERS: t0 t1 t2 a2
load_get_word:
psh rp
psh fp
mov fp sp
	psh s1
	nul a1
	mld s1 load_state
	jmp .exit s1.neg
	mld t1 load_buffer

	jnl load_convert_twochar
	jmp .end a2.nzr
	mov t2 a1

	mov t0 s1
	inc t0
	lbl t0 2
	lbr t2 t0
	ior t1 t2

	lsi t0 3
	sub t0 s1
	lbl t0 2
	lbl a1 t0

	jmp .skip s1.nzr
		lsi s1 3
		jnl load_convert_twochar
		jmp .skip a2.zer
	.end:
	nul t1
	nul a1
	nul s1
	.skip:
	dec s1
	mst s1 load_state
	mst a1 load_buffer
	mov a1 t1
	.exit:
	pop s1
mov sp fp
pop fp
pop rp
jmp rp

load_buffer: #res 1
load_state: #res 1

invalid_text:
#d16 .end-$-1
#d "\tCommand parameter(s) are invalid.\r\n"
#align 16
.end:

;__________________________________________________

clear_text:
#d16 .end-$-1
#d 0x1B5B`16, 0x324A`16, 0x1B5B`16, 0x4800`16
#align 16
.end:

read_text:
#d16 .end-$-1
#d "\tValue: "
#align 16
.end:

load_text:
#d16 .end-$-1
#d " words loaded and executed.\r\n"
#align 16
.end:

quit_text:
#d16 .end-$-1
#d "\tGoodbye!\r\n"
#align 16
.end:

help_text:
#d16 .end-$-1
#d "\tAvailable Commands:\r\n"
#d "\t____________________________________________________\r\n"
#d "\trd\tReads and prints back the word at CA\r\n"
#d "\twr HX\tWrites the given word to CA\r\n"
#d "\r\n"
#d "\tca HX\tUpdates CA to the given word\r\n"
#d "\tgo\tTransfers execution to CA\r\n"
#d "\r\n"
#d "\tdm HX\tPrints out a given amount of words\r\n"
#d "\tld\tLoads and executes a p64-encoded binary\r\n"
#d "\r\n"
#d "\thl\tPrints this list\r\n"
#d "\tcs\tClears the screen\r\n"
#d "\tqt\tHalts the processor\r\n"
#d "\r\n"
#d "\tDisambiguations:\r\n"
#d "\t____________________________________________________\r\n"
#d "\tCA\tCurrent Address; The number visible at the\r\n"
#d "\t\tstart of the prompt. Most commands increase\r\n"
#d "\t\tthis value by the amount of words that they\r\n"
#d "\t\thave processed.\r\n"
#d "\tHX\tHex Extended; a 16 bits hexadecimal number\r\n"
#align 16
.end:

;__________________________________________________