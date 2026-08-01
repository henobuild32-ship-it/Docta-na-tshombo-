Set-Location "c:\360\Docta-na-tshombo-main"
$output = & .\gradlew.bat compileDebugKotlin --no-daemon 2>&1
$output | Out-File -FilePath "compilation_output.txt" -Encoding UTF8
Write-Output "Compilation terminée. Voir compilation_output.txt"
