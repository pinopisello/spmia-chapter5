package com.thoughtmechanix.licenses.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.thoughtmechanix.licenses.clients.OrganizationRestTemplateClient;
import com.thoughtmechanix.licenses.config.ServiceConfig;
import com.thoughtmechanix.licenses.model.License;
import com.thoughtmechanix.licenses.model.Organization;
import com.thoughtmechanix.licenses.repository.LicenseRepository;
import com.thoughtmechanix.licenses.utils.UserContextHolder;

@Service
public class LicenseService {
    private static final Logger logger = LoggerFactory.getLogger(LicenseService.class);
    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    ServiceConfig config; //contiene property example.property definita config server.

    @Autowired
    OrganizationRestTemplateClient organizationRestClient;

    @HystrixCommand( threadPoolKey = "licenseByOrgThreadPoolPeppe")
    public License getLicense(String organizationId,String licenseId) {
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);

        Organization org = getOrganization(organizationId);

        return license
                .withOrganizationName( org.getName())
                .withContactName( org.getContactName())
                .withContactEmail( org.getContactEmail() )
                .withContactPhone( org.getContactPhone() )
                .withComment(config.getExampleProperty());
    }

   
    private Organization getOrganization(String organizationId) {
        return organizationRestClient.getOrganization(organizationId);
    }

    private void randomlyRunLong(){
      Random rand = new Random();

      int randomNum = rand.nextInt(3) + 1;//ritorna un intero tra 1-3

      System.out.println("randomNum : "+randomNum);
      
      if (randomNum==3)
    	  try {
              Thread.sleep(11000);
          } catch (InterruptedException e) {
              e.printStackTrace();
          }
    	  
    }
 
    
    //@HystrixCommand    //caso 1:timeout 1000ms
    /*@HystrixCommand(   //caso 2 : timeout 12000ms (il metodo ritorna sempre successo)
    		  commandProperties=
    		       {@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="12000")}
    )*/
    
    //@HystrixCommand(fallbackMethod = "buildFallbackLicenseList") //caso 3 : fallback metodo in caso eccezzione
    
   /* @HystrixCommand(//caso 4: Bulkhead pattern per usare diversi threadpools.
    threadPoolKey = "licenseByOrgThreadPoolPippo",
    threadPoolProperties =
            {@HystrixProperty(name = "coreSize",value="2"),
             @HystrixProperty(name="maxQueueSize", value="10")},
            commandProperties=
	       {@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="12000")}
    )
    */
    
    @HystrixCommand(//caso 5: Bulkhead pattern con commandPoolProperties per configurare il comportamento breaker.
    	    threadPoolKey = "licenseByOrgThreadPoolPippo",
    	    threadPoolProperties =
    	            {@HystrixProperty(name = "coreSize",value="3"),
    	             @HystrixProperty(name="maxQueueSize", value="10")}
    	            ,commandProperties=
    		              { @HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="12000"),
    		    		    @HystrixProperty(name="circuitBreaker.requestVolumeThreshold", value="10"),
    		    		    @HystrixProperty(name="circuitBreaker.errorThresholdPercentage", value="75"),
    		    		    @HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds", value="7000"),
    		    		    @HystrixProperty(name="metrics.rollingStats.timeInMilliseconds", value="15000"),
    		    		    @HystrixProperty(name="metrics.rollingStats.numBuckets", value="5")})
    public List<License> getLicensesByOrg(String organizationId){
        logger.debug("LicenseService.getLicensesByOrg  Correlation id: {}", UserContextHolder.getContext().getCorrelationId());
        randomlyRunLong();
        return licenseRepository.findByOrganizationId(organizationId);
    }

    private List<License> buildFallbackLicenseList(String organizationId){
        List<License> fallbackList = new ArrayList<>();
        License license = new License()
                .withId("0000000-00-00000")
                .withOrganizationId( organizationId )
                .withProductName("Sorry no licensing information currently available");

        fallbackList.add(license);
        return fallbackList;
    }

    public void saveLicense(License license){
        license.withId( UUID.randomUUID().toString());

        licenseRepository.save(license);
    }

    public void updateLicense(License license){
      licenseRepository.save(license);
    }

    public void deleteLicense(License license){
        licenseRepository.delete( license.getLicenseId());
    }

}
