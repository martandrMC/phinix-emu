fibonacci:
	lsi s3 1
	.loop:
		mov s1 s2
		mov s2 s3
		out s3 dbgcon
		add s3 s1
	jmp ip .loop nc
jmp rp