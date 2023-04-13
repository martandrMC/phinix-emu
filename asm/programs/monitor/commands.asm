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
	lfi a4 unknown_text
	jmp print_packed_string

cmd_load:
	lfi a4 unknown_text
	jmp print_packed_string

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
	lfi a4 goodbye_text
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

goodbye_text:
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
#d "\t\r\n"
#d "\tca HX\tUpdates CA to the given word\r\n"
#d "\tgo\tTransfers execution to CA\r\n"
#d "\t\r\n"
#d "\thl\tPrints this list\r\n"
#d "\tcs\tClears the screen\r\n"
#d "\tqt\tHalts the processor\r\n"
#d "\t\r\n"
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