ttyraw = 0xFE
lsi t0 3			; CTRL+C
mov rf t0
loop:
	inp t0 ttyraw
	prd t0.ref
	hlt
	out t0 ttyraw
jmp ip loop