import com.github.rnorth.blitzemj.model.Node
import com.github.rnorth.blitzemj.model.LoadBalancer
import static com.github.rnorth.blitzemj.model.Size.*

defaults["osVersion"] = "11.04"

new LoadBalancer([
	name:"web-lb1",
	tags:["web"]
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