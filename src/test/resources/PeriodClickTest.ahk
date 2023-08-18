LoopFunction()
{
    while true
    {
        while GetKeyState("U", "P")
        {
            ; Send "u"
            ; Send "u"
            Click "Down"
            Sleep 1
            Click "Up"
        }
        Sleep 1000
    }
}

LoopFunction()

$U::
{
    ;Blocker
}