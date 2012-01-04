import com.github.rnorth.blitzemj.model.*
import static com.github.rnorth.blitzemj.model.Size.*


Defaults.DEFAULTS["osVersion"] = "11.04"
Defaults.DEFAULTS["provisioning"] = [new ScriptExecution("apt-get update; apt-get install dtach; mkdir web; cd web; hostname > index.html; dtach -n /tmp/simple_http.worker python -m SimpleHTTPServer 8080")]

new LoadBalancer([
	name:"web-lb1",
	tags:["web"],
	appliesToTag: "web",
	protocol: "http",
	port: 80,
	nodePort: 8080
	])

new Node([
	name:"web1", 
	tags:["web", "peak"], 
	size:ram(512)
	])

new Node([
	name:"web2", 
	tags:["web", "peak"], 
	size:ram(256)
	])