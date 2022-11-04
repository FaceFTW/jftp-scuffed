^+1::
	SetTitleMatchMode, 1
	CoordMode, Mouse, Window
	mainWin := WinExist("JFtp")
	WinActivate, "JFtp"
	MouseMove, 25, 65
	MouseClick, Left
	Sleep, 1000
	WinActivate, "Ftp Connection"
	MouseMove, 353, 102
	MouseClick, Left
	Sleep, 100
	Send, super_secure_password
	Sleep, 100
	MouseMove, 37, 270
	MouseClick, Left
	Sleep, 100
	MouseMove, 447, 367
	MouseClick, Left
	Sleep, 1000
	WinActivate, "JFtp"
	MouseMove, 68, 299
	MouseClick, Left, , , 2
	Sleep, 500
	Send, {Control down}
	MouseMove, 68, 238
	MouseClick, Left
	Sleep, 250
	MouseMove, 68, 257
	MouseClick, Left
	Send, {Control up}
	Sleep, 500
	MouseMove, 426, 168
	MouseClick, Left
