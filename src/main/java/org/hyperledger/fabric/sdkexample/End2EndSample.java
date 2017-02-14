package org.hyperledger.fabric.sdkexample;

import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.events.EventHub;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class End2EndSample {
    private final static Config config = Config.getConfig();

    final static Collection<String> PEER_LOCATIONS = Arrays.asList(config.getPeerAddress());

    final static Collection<String> ORDERER_LOCATIONS = Arrays.asList(config.getSoloOrdererAddress()); //Vagrant maps to this

    final static Collection<String> EVENTHUB_LOCATIONS = Arrays.asList(config.getEventAddress()); //Vagrant maps to this

    final static String FABRIC_CA_SERVICES_LOCATION = config.getCaAddress();

    public static void main(String[] args){
        End2EndSample sample = new End2EndSample();
        sample.setup();
    }

    public void setup() {

        HFClient client = HFClient.createNewInstance();
        try {

            //////////////////////////// TODo Needs to be made out of bounds and here chain just retrieved
            //Construct the chain
            //

            constructChain(client);

            client.setUserContext(new User(config.getUserName())); // User will be defined by pluggable

            Chain chain = client.getChain(config.getChainId());

            chain.setDeployWaitTime(config.getDeployWaittime());
            chain.setInvokeWaitTime(config.getInvokeWaittime());

            chain.setMemberServicesUrl(FABRIC_CA_SERVICES_LOCATION, null);

            File fileStore = new File(System.getProperty("user.home") + "/"+config.getKeystoreProperties());
            if (fileStore.exists()) {
                fileStore.delete();
            }
            chain.setKeyValStore(new FileKeyValStore(fileStore.getAbsolutePath()));
            chain.enroll(config.getUserName(), config.getUserPswd());

            chain.initialize();

            Collection<Peer> peers = chain.getPeers();
            Collection<Orderer> orderers = chain.getOrderers();

            ////////////////////////////
            //Deploy Proposal Request
            //

            out("Creating deployment proposal");

            DeploymentProposalRequest deploymentProposalRequest = client.newDeploymentProposalRequest();
            deploymentProposalRequest.setChaincodeName(config.getChainCodeName());
            deploymentProposalRequest.setChaincodePath(config.getChainCodePath());
            deploymentProposalRequest.setFcn(config.getDeployFunction());
            deploymentProposalRequest.setArgs(config.getDeployArgs());
            out("Deploying chain code with a and b set to 100 and 200 respectively");


            Collection<ProposalResponse> responses = chain.sendDeploymentProposal(deploymentProposalRequest, peers);

            //////////////////////
            //Deploy Transaction
            //

            Collection<ProposalResponse> successful = new LinkedList<>();
            Collection<ProposalResponse> failed = new LinkedList<>();


            for (ProposalResponse response : responses) {
                //FIXME: response.isVerified() fails as peercert is not maintained part of fabricsdk jar hence the call fails
                //if (response.isVerified() && response.getStatus() == ProposalResponse.Status.SUCCESS) {
                if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                    successful.add(response);
                } else {
                    failed.add(response);
                }

            }
            out("Received %d deployment proposal responses. Successful+verified: %d . Failed: %d", responses.size(), successful.size(), failed.size());

            if (successful.size() < 1) {  //choose this as an arbitrary limit right now.

                if (failed.size() == 0) {
                    throw new Exception("No endorsers found ");

                }
                ProposalResponse first = failed.iterator().next();

                //TODO: Check whu unable to convert response to message
                throw new Exception("Not enough endorsers :" + successful.size() + ".  " + first.getProposalResponse().getResponse().getMessage());
            }
            ProposalResponse firstDeployProposalResponse = successful.iterator().next();
            final ChainCodeID chainCodeID = firstDeployProposalResponse.getChainCodeID();


            chain.sendTransaction(successful, orderers).thenApply(block -> {

                try {

                    out("Successfully completed chaincode deployment.");

                    out("Creating invoke proposal");

                    InvokeProposalRequest invokeProposalRequest = client.newInvokeProposalRequest();

                    invokeProposalRequest.setChaincodeID(chainCodeID);
                    invokeProposalRequest.setFcn(config.getInvokeFunction());
                    invokeProposalRequest.setArgs(config.getInvokeArgs());

                    Collection<ProposalResponse> invokePropResp = chain.sendInvokeProposal(invokeProposalRequest, peers);


                    successful.clear();
                    failed.clear();

                    for (ProposalResponse response : invokePropResp) {

                        if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                            successful.add(response);
                        } else {
                            failed.add(response);
                        }

                    }
                    out("Received %d invoke proposal responses. Successful+verified: %d . Failed: %d", responses.size(), successful.size(), failed.size());


                    if (successful.size() < 1) {  //choose this as an arbitrary limit right now.

                        if (failed.size() == 0) {
                            throw new Exception("No endorsers found ");

                        }
                        ProposalResponse firstDeployProposalResponse2 = failed.iterator().next();


                        throw new Exception("Not enough endorsers :" + successful.size() + ".  " + firstDeployProposalResponse2.getMessage());


                    }
                    out("Successfully received invoke proposal response.");

                    ////////////////////////////
                    // Invoke Transaction
                    //

                    out("Invoking chain code to move 100 from a to b.");

                    return chain.sendTransaction(successful, orderers).get(60, TimeUnit.SECONDS);


                } catch (Exception e) {

                    throw new RuntimeException(e);

                }


            }).thenApply(block -> {
                try {
                    //out("Successfully ordered invoke chain code. BlockClass" + block.getClass());


                    ////////////////////////////
                    // Query Proposal
                    //


                    out("Now query chain code for the value of b.");


                    // InvokeProposalRequest qr = InvokeProposalRequest.newInstance();
                    QueryProposalRequest queryProposalRequest = client.newQueryProposalRequest();

                    queryProposalRequest.setArgs(config.getQueryArgs());
                    queryProposalRequest.setFcn("invoke");
                    queryProposalRequest.setChaincodeID(chainCodeID);


                    Collection<ProposalResponse> queryProposals = chain.sendQueryProposal(queryProposalRequest, peers);

                    for (ProposalResponse proposalResponse : queryProposals) {
                        //FIXME: response.isVerified() fails as peercert is not maintained part of fabricsdk jar hence the call fails
                        //if (!proposalResponse.isVerified() || proposalResponse.getStatus() != ProposalResponse.Status.SUCCESS) {
                        if (proposalResponse.getStatus() != ProposalResponse.Status.SUCCESS) {
                            return new Exception("Failed invoke proposal.  status: " + proposalResponse.getStatus() + ". messages: " + proposalResponse.getMessage());

                        }

                    }

                    out("Successfully received query response.");
                    //TODO: resolve the issue and uncomment below
                    //String payload = "300";//queryProposals.iterator().next().getProposalResponse().getResponse().getPayload().toStringUtf8();
                    String payload = queryProposals.iterator().next().getProposalResponse().getResponse().getPayload().toStringUtf8();

                    out("Query payload of b returned %s", payload);

                    return null;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }).exceptionally(e -> {
                System.err.println("Bad status value for proposals transaction: " + e.getMessage());
                System.exit(8);
                return null;
            }).get(90, TimeUnit.SECONDS);
            //out("That's all folks!");
            out("Completed End to End Demo ...");
            System.exit(0);


        } catch (Exception e) {
            out("Caught an exception");
            e.printStackTrace();

            //Assert.fail(e.getMessage());

        }

    }


    private static void constructChain(HFClient client) throws Exception {
        //////////////////////////// TODo Needs to be made out of bounds and here chain just retrieved
        //Construct the chain
        //

        Chain newChain = client.newChain(config.getChainId());

        for (String peerloc : PEER_LOCATIONS) {
            Peer peer = client.newPeer(peerloc);
            peer.setName("peer1");
            newChain.addPeer(peer);
        }

        for (String orderloc : ORDERER_LOCATIONS) {
            Orderer orderer = client.newOrderer(orderloc);
            newChain.addOrderer(orderer);
        }

        for (String eventHub : EVENTHUB_LOCATIONS) {
            EventHub orderer = client.newEventHub(eventHub);
            newChain.addEventHub(orderer);
        }

    }


    static void out(String format, Object... args) {

        System.err.flush();
        System.out.flush();

        System.out.println(String.format(format, (Object[]) args));
        System.err.flush();
        System.out.flush();

    }
}
