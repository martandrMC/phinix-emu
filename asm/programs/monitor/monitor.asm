;__________________________________________________

dbgcon = 0x00
ttyraw = 0xFE
ttypkd = 0xFF

;__________________________________________________

start:
	; setup the stack
	nul sp
	lsi fp -1
	psh fp

	nul s7					; s7 contains the current address
	lfi a4 title_text		; load the title's memory address
	jnl print_packed_string	; print the title string
cmdloop:
	mov a1 s7
	jnl print_hex_word	; print current address
	lfi t0 0x3E20		; load the twochar "> "
	out t0 ttypkd		; print it

	inp s1 ttypkd		; s1 contains twochar cmd name
	cmp s1 0x0D0A		; if an empty line was read
	jmp ip cmdloop eq	; ignore it
	mov a1 s1			; put twochar in parameter 1
	jnl hash			; hash the twochar to get unique index

	lfi s6 short_cmds_table	; load table base to pointer register
	and a1 0x000F			; retain lower nibble of the hashed name
	lsl a1 a1				; mult by 2, as table elements are 2 words
	add s6 a1				; add calculated offset to pointer

	mld t0 s6+					; load the expected name
	lfi a4 unknown_text			; load parameter for print func
	lfi t1 print_packed_string	; load print func address as default action
	cmp t0 s1					; compare the typed name with the expected
	prd eq						; if they are equal
	mld t1 s6					; overwrite default with table's func address
	jnl t1						; call the function
	jnl empty_input				; clear remaining characters
jmp ip cmdloop

;__________________________________________________

hash:
	nul t0
	lsi t1 5
	.loop:
		xor t0 a1
		lbr a1 3
		inc t0
	dec t1
	jmp .loop t1.nzr
	mov a1 t0
jmp rp

short_cmds_table:
#d 0`16, 0`16
#d "hl", cmd_help`16
#d "cs", cmd_clear`16
#d 0`16, 0`16
#d "dm", cmd_dump`16
#d "qt", cmd_quit`16
#d "ca", cmd_addr`16
#d 0`16, 0`16
#d "wr", cmd_write`16
#d 0`16, 0`16
#d "rd", cmd_read`16
#d 0`16, 0`16
#d 0`16, 0`16
#d 0`16, 0`16
#d "ld", cmd_load`16
#d "go", cmd_jump`16

;__________________________________________________

title_text:
#d16 .end-$-1
#d "\x1B[2JPHINIX+ Machine Code Monitor v0.3\r\n"
#d "Type ", 34`8, "hl", 34`8, " for a command listing.\r\n"
#d "User memory starts at hex 0400\r\n\r\n"
#align 16
.end:

unknown_text:
#d16 .end-$-1
#d "\tTyped command is unknown.\r\n"
#align 16
.end:

;__________________________________________________

#include "tty_utils.asm"
#include "commands.asm"
;#addr 0x1000
;#include "tests/fibonacci.asm"
;#addr 0x1010
;#include "tests/collatz.asm"

;__________________________________________________