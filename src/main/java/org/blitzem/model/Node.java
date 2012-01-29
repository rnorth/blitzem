package org.blitzem.model;

import static org.jclouds.compute.options.TemplateOptions.Builder.authorizePublicKey;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.blitzem.TaggedAndNamedItem;
import org.blitzem.TaggedItemRegistry;
import org.blitzem.commands.CommandException;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeState;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.options.TemplateOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

/**
 * Model class for a server node.
 * 
 * @author Richard North <rich.north@gmail.com>
 * 
 */
public class Node implements TaggedAndNamedItem {

	private static final Logger CONSOLE_LOG = LoggerFactory.getLogger(Node.class);

	private String name = null;
	private List<String> tags = Lists.newArrayList();
	private Size size = Size.DEFAULT;
	private Os os = Os.DEFAULT;
	private List<? extends Provisioning> provisioning = (List<? extends Provisioning>) Defaults.DEFAULTS.get("provisioning");

	public Node() {
		TaggedItemRegistry.getInstance().add(this);
	}

	public Node(String name, String[] tags) {
		this();
		this.name = name;
		this.tags.addAll(Arrays.asList(tags));
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Node [name=").append(name).append(", tags=").append(tags).append(", size=").append(size).append(", os=").append(os)
				.append("]");
		return builder.toString();
	}

	
	/** 
	 * {@inheritDoc}
	 */
	public String getName() {
		return name;
	}

    /**
     * {@inheritDoc}
     */
    public Set<String> getNotificationSubjects() {
        return Sets.newHashSet();
    }

    /**
     * {@inheritDoc}
     */
    public void notifyIsUp(TaggedAndNamedItem itemWhichIsUp, ExecutionContext executionContext) {
        // Don't care
    }

    /**
     * {@inheritDoc}
     */
    public void notifyIsGoingDown(TaggedAndNamedItem itemWhichIsGoingDown, ExecutionContext executionContext) {
        // Don't care
    }

    /** 
     * {@inheritDoc}
     */
    public boolean isUp(ExecutionContext executionContext) {
        return ! Node.findExistingNodesMatching(this, executionContext.getComputeService()).isEmpty();
    }

    /**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/** 
	 * {@inheritDoc}
	 */
	public List<String> getTags() {
		return tags;
	}

	/**
	 * @param tags the tags to set
	 */
	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	/**
	 * @return the size
	 */
	public Size getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(Size size) {
		this.size = size;
	}

	/**
	 * @return the os
	 */
	public Os getOs() {
		return os;
	}

	/**
	 * @param os the os to set
	 */
	public void setOs(Os os) {
		this.os = os;
	}

	/**
	 * @return the provisioning
	 */
	public List<? extends Provisioning> getProvisioning() {
		return provisioning;
	}

	/**
	 * @param provisioning the provisioning to set
	 */
	public void setProvisioning(List<? extends Provisioning> provisioning) {
		this.provisioning = provisioning;
	}

	public void preUp(ExecutionContext executionContext) {
		// TODO Auto-generated method stub
		
	}

	public void up(ExecutionContext executionContext) throws CommandException {

        ComputeService computeService = executionContext.getComputeService();

        final TemplateBuilder templateBuilder = computeService
				.templateBuilder();
        
		TemplateOptions options = new TemplateOptions();
		File sshKeyFile = null;
		try {
			sshKeyFile = new File(System.getProperty("user.home") + "/.ssh/id_rsa.pub");
			options  = authorizePublicKey(Files.toString(sshKeyFile, Charsets.UTF_8));
		} catch (IOException e) {
			CONSOLE_LOG.error("Could not load public SSH key from file: {}. Please create an SSH public/private key pair (with `ssh-keygen -t rsa`) and try again.", sshKeyFile.getAbsoluteFile());
			throw new RuntimeException(e);
		}
		
		for (Provisioning provisioning : this.getProvisioning()) {
			provisioning.asTemplateOption().copyTo(options);
		}
		
		templateBuilder
			.minRam(this.getSize().getMinRam())
			.minCores(this.getSize().getMinCores())
			.osFamily(OsFamily.valueOf(this.getOs().getFamily()))
			.osVersionMatches(this.getOs().getVersion())
			.os64Bit(this.getOs().getOs64Bit())
			.options(options);
		
		Template template = templateBuilder.build();
		CONSOLE_LOG.info("Creating node with template: {}", template);
		try {
			Set<? extends NodeMetadata> nodesInGroup = computeService.createNodesInGroup(this.getName(), 1, template);
			for (NodeMetadata createdNode : nodesInGroup) {
				String loginUsername = createdNode.getCredentials().getUser();
				CONSOLE_LOG.info("Created node {} at {}. Standard login username is "+ loginUsername, this.getName(), createdNode.getPublicAddresses());
			}

		} catch (RunNodesException e) {
			throw new CommandException("Could not create node!", e);
		}
	}

    public void postUp(ExecutionContext executionContext) {
        Set<LoadBalancer> lbsToNotify = findLoadBalancersToNotifyOfChange();

        for (LoadBalancer lb : lbsToNotify) {
            lb.notifyIsUp(this, executionContext);
        }
	}

	/**
	 * Find an existing live node ({@link NodeMetadata}) which matches 1:1 with
	 * a modelled {@link Node} in the local environment spec.
	 *
	 * @param node
	 *            the modelled Node
	 * @param computeService
	 *            for looking up live nodes
	 * @return a set of matching {@link NodeMetadata}s - usually expect just
	 *         one, but more could be found and should be handled.
	 */
	public static Set<? extends NodeMetadata> findExistingNodesMatching(final Node node, final ComputeService computeService) {
        return computeService.listNodesDetailsMatching(new Predicate<ComputeMetadata>() {

            public boolean apply(ComputeMetadata arg0) {
                
            	final NodeState state = computeService.getNodeMetadata(arg0.getId()).getState();
				boolean isUp = (state == NodeState.RUNNING) || (state == NodeState.PENDING);
            	final String name = arg0.getName();

                if (name.contains("-")) {
                    String trimmedName = name.substring(0, name.lastIndexOf('-'));
                    return trimmedName.equals(node.getName()) && isUp;
                } else {
                    return name.equals(node.getName()) && isUp;
                }

            }

        });
	}

    public void preDown(ExecutionContext executionContext) {
        Set<LoadBalancer> lbsToNotify = findLoadBalancersToNotifyOfChange();

        for (LoadBalancer lb : lbsToNotify) {
            lb.notifyIsGoingDown(this, executionContext);
        }
    }

	public void down(ExecutionContext executionContext) {

        ComputeService computeService = executionContext.getComputeService();

        Set<? extends NodeMetadata> existingNodes = Node.findExistingNodesMatching(this, computeService);

		for (NodeMetadata existingNode : existingNodes) {
			CONSOLE_LOG.info("Bringing down node {}", existingNode.getName());
			computeService.destroyNode(existingNode.getId());
			CONSOLE_LOG.info("Node destroyed");
		}
	}

	public void postDown(ExecutionContext executionContext) {
		// TODO Auto-generated method stub

	}

    private Set<LoadBalancer> findLoadBalancersToNotifyOfChange() {
        Set<LoadBalancer> lbsToNotify = Sets.newHashSet();
        lbsToNotify.addAll( TaggedItemRegistry.getInstance().findReceivesNotificationsFor(this.getName(), LoadBalancer.class) );
        for (String tag : this.getTags()) {
            lbsToNotify.addAll( TaggedItemRegistry.getInstance().findReceivesNotificationsFor(tag, LoadBalancer.class) );
        }
        return lbsToNotify;
    }


}
