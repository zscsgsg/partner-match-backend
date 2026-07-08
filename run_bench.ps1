param([string]$MainClass = "com.zsc.partnermatch.ManualBenchmark")
$cp = (Get-Content target\classpath.txt -Raw).Trim()
$fullCp = "target\test-classes;target\classes;$cp"
java -cp $fullCp $MainClass
