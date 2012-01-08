package org.blitzem.model;

import org.jclouds.compute.ComputeService;
import org.jclouds.loadbalancer.LoadBalancerService;

/**
 * Created by IntelliJ IDEA.
 * User: richardnorth
 * Date: 07/01/2012
 * Time: 17:24
 * To change this template use File | Settings | File Templates.
 */
public class ExecutionContext {

    private ComputeService computeService;
    private LoadBalancerService loadBalancerService;

    public ExecutionContext(ComputeService computeService) {
        this.computeService = computeService;
    }

    public ExecutionContext(ComputeService computeService, LoadBalancerService loadBalancerService) {
        this.loadBalancerService = loadBalancerService;
        this.computeService = computeService;
    }

    public ComputeService getComputeService() {
        return computeService;
    }

    public void setComputeService(ComputeService computeService) {
        this.computeService = computeService;
    }

    public LoadBalancerService getLoadBalancerService() {
        return loadBalancerService;
    }

    public void setLoadBalancerService(LoadBalancerService loadBalancerService) {
        this.loadBalancerService = loadBalancerService;
    }
}
