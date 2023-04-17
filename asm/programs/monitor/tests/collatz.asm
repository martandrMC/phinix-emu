; p64-econded binary: p+41004A0@0@b1^h0M1m2`2d@<g`\@77Bm@=]42`giL;_M2a0D\0c8H0!!
collatz:
	nul s2
	lsi s1 27
	lsi s3 1
	mov rf s3
	.loop:
		out s1 dbgcon
		inc s2
		jmp .even s1.evn
		lsl s3 s1
		add s1 s3
		inc s1
		jmp ip .loop
		.even:
		lsr s1 s1
	jmp .loop s1.nrf
	out s2 0
jmp rp