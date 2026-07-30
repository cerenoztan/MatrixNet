import java.io.*;
import java.util.Locale;
//Ceren Öztan 2023400288
public class Main {
    //MatrixNet is the class that graph's edges and vertices are created and every interaction is implemented
    private static MatrixNet MatrixNet=new MatrixNet();

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        if (args.length != 2) {
            System.err.println("Usage: java Main <input_file> <output_file>");
            System.exit(1);
        }
        //the input and output file
        String inputFile = args[0];
        String outputFile = args[1];

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                //processing each line at switch case
                processCommand(line, writer);
            }

        } catch (IOException e) {
            System.err.println("Error reading/writing files: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private static void processCommand(String command, BufferedWriter writer)
            throws IOException {
        String[] parts = command.split("\\s+");
        String operation = parts[0];

        try {
            String result = "";
            switch (operation) {
                case("spawn_host"):
                {
                    //for spawnHost method of MatrixNet hostID and clearance level is important
                    String hostID=parts[1];
                    int clearance=Integer.parseInt(parts[2]);
                    writer.write(MatrixNet.spawnHost(hostID,clearance));
                    //when the case is implemented the break occurs
                    break;
                }
                case("link_backdoor"):
                {
                    //for linkBackdoor method of MatrixNet both hostID's and
                    // backdoor's latency, firewall,bandwidth values are important
                    String hostID1=parts[1];
                    String hostID2=parts[2];
                    int latency=Integer.parseInt(parts[3]);
                    int bandwidth=Integer.parseInt(parts[4]);
                    int firewall=Integer.parseInt(parts[5]);
                    writer.write(MatrixNet.linkBackdoor(hostID1,hostID2,latency,
                            bandwidth,firewall));

                    break;

                }
                case("seal_backdoor"):{
                    //for sealBackdoor method of MatrixNet hostID's are important
                    String hostID1=parts[1];
                    String hostID2=parts[2];
                    writer.write(MatrixNet.sealBackdoor(hostID1,hostID2));
                    break;

                }
                case("trace_route"):{
                    //for traceRoute method of MatrixNet source, destination and requirement of bandwidth
                    //and the lambda that changes the whole optimal route results are important
                    String sourceID=parts[1];
                    String destinationID=parts[2];
                    int minBandwidth=Integer.parseInt(parts[3]);
                    int lambda=Integer.parseInt(parts[4]);
                    writer.write(MatrixNet.traceRoute(sourceID,destinationID,minBandwidth,lambda));
                    break;
                }
                case("scan_connectivity"):{
                    //for scanConnectivity method of MatrixNet the connectivity according to the unsealed
                    //backdoors important so no parameter only the available backdoors
                    writer.write(MatrixNet.scanConnectivity());
                    break;
                }
                case("simulate_breach"):{
                    //simulate breach has two probabilities: host breach and backdoor breach
                    //when it is a host breach only method name + hostID is inside the parts
                    if(parts.length==2){
                        String hostID=parts[1];
                        //for hostBreach method of MatrixNet hostID is important
                        writer.write(MatrixNet.hostBreach(hostID));
                    }
                    //when it is a backdoor breach  method name + hostID1 + hostID2 is in the parts
                    // 2 hosts identify a backdoor
                    if(parts.length==3) {
                        String hostID1 = parts[1];
                        String hostID2=parts[2];
                        //for backdoorBreach method of MatrixNet hostIDs are important
                        writer.write(MatrixNet.backdoorBreach(hostID1,hostID2));
                    }
                    break;
                }
                //oracle report is a string output that transmits information about current situation
                case("oracle_report"):{
                    writer.write(MatrixNet.oracleReport());
                    break;
                }
                //used for safety
                default:
                    result = "Unknown command: " + operation;
                    writer.write(result+"\n");
            }

        } catch (Exception e) {

            writer.write("Error processing command: " + command);
            writer.newLine();
        }
    }
}