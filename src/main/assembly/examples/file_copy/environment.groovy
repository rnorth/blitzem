import org.blitzem.model.*
import org.blitzem.model.thirdparty.*
import static org.blitzem.model.Size.*


Defaults.DEFAULTS["osVersion"] = "11.04"
Defaults.DEFAULTS["provisioning"] = [
	new FileCopy([
		from:"sample-index.html",
		to:"/tmp/index.html"
	]),
	new ScriptExecution("apt-get update; apt-get install dtach; mkdir /tmp/web; cd /tmp/web; cp ../index.html .; dtach -n /tmp/simple_http.worker python -m SimpleHTTPServer 8080")
]

new Node([
	name:"web1", 
	tags:["web", "peak"], 
	size:ram(256)
	])
