print_packed_string:

jmp rp

; Prints the word given in register a1 in hex
; ARGUMENTS: a1
; CLOBERS: t0 t1 a1
print_hex_word:
    lsi t0 4				; loop 4 times
    .loop:
        pen t1 a1 0xCCC3	; move uppermost nibble of a1 to t1
        add t1 0x0030		; add '0' to t1
        cmp t1 0x003A		; compare t1 with ':'
        prd ae				; if it's above or equal
        ads t1 6			; add 7 to end up at 'A'
        out t1 ttyraw		; print the character
        lbl a1 4			; shift a1 up 4 bits
    dec t0					; decrement loop counter
    jmp .loop t0.nzr		; repeat until counter is zero
jmp rp

; Reads in a non-whitespace character in a1
; a2 will contain the amount of whitesapce consumed
; RETURNS: a1 a2
read_nonws:
	lsi a2 -1				; init the whitespace counter
	.loop:
		inp a1 ttyraw		; read a raw character
		inc a2				; increment the counter
	cmp a1 32				; check equal with space
	jmp ip .loop eq			; skip if space
	cmp a1 9				; check equal with tab
	jmp ip .loop eq			; skip if tab
jmp rp

; Reads a 4-character hexadecimal number in a1
; a2 will be non-zero if the read was successful
; RETURNS: a1 a2
; CLOBERS: t0 t1 rf.0
read_hex_word:
psh rp
psh fp
mov fp sp
	jnl read_nonws			; read first character
	mov t1 a1				; move it in place
	lsi t0 3				; loop 4 times
	nul a1					; zero the returned value
	nul a2					; default no error
	.loop:
		rbd rf.0 zr.zer		; put a 1 in rf.0
		cmp t1 0x0030		; compare the char with '0'
		rbc rf.0 ae			; && if the char is >= '0'
		cmp t1 0x003A		; compare the char with ':'
		rbc rf.0 bl			; && if the char is < ':'
		bxt rf.0			; put the result in the carry
		jmp ip .done cr		; if cond true then done

		bxt t1.6			; hacky way to do toUpper
		bdp t1.5			; moves bit 7 to bit 6
		sub t1 0x0061		; subtract 'a'
		cmp t1 0x0006		; if the value is >= 6
		jmp ip .error ae	; then its an invalid char
		
		add t1 0x003A		; add ':' to reposition above '9'
		.done:
		sub t1 0x0030		; subtract '0' to get to [0,15]
		lbl a1 4			; make space for the new nibble
		ior a1 t1			; insert the nibble in
		jmp .end t0.zer		; exit the loop if the counter is zero
		inp t1 ttyraw		; read a new character otherwise
	dec t0					; decrement loop counter
	jmp ip .loop			; repeat
	.error:
	lsi a2 1				; make a2 non-zero
	.end:
mov sp fp
pop fp
pop rp
jmp rp

empty_input:

jmp rp
