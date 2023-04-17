; p64-encoded binary: p+400021000@b07@7;0Mb`3D2mm?c8H0!!
fibonacci:
	nul s2
	lsi s3 1
	.loop:
		mov s1 s2
		mov s2 s3
		out s3 dbgcon
		add s3 s1
	jmp ip .loop nc
jmp rp