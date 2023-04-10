add t0 zr 0xFF
outter:
	add t5 zr message
	inner:
		mld t1 t5+
		out t1 0xFF
		and t1 t0
	jmp ip inner-$ nz
jmp ip outter-$

message:
#d "Hello!\r\n"
#align 16