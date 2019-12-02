package burp;

import java.io.PrintWriter;
import java.util.Random;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BurpExtender implements burp.IBurpExtender, burp.IHttpListener
{
    private burp.IExtensionHelpers helpers;
    private PrintWriter stdout;
    private PrintWriter stderr;

    private int counter = 0;

    //
    // implement IBurpExtender
    //
    @Override
    public void registerExtenderCallbacks(burp.IBurpExtenderCallbacks callbacks)
    {
        // obtain an extension helpers object
        helpers = callbacks.getHelpers();
        stdout = new PrintWriter(callbacks.getStdout(), true);
        stderr = new PrintWriter(callbacks.getStderr(),true);

        // set our extension name
        callbacks.setExtensionName("IncrementItPlease");

        // register ourselves as an HTTP listener
        callbacks.registerHttpListener(this);
    }

    //
    // implement IHttpListener
    //
    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, burp.IHttpRequestResponse messageInfo)
    {
        boolean updated = false;

        // only process requests
        if (messageIsRequest) {
            // get the HTTP service for the request
            burp.IHttpService httpService = messageInfo.getHttpService();
            burp.IRequestInfo iRequest = helpers.analyzeRequest(messageInfo);

            String request = new String(messageInfo.getRequest());

            List<String> headers = iRequest.getHeaders();
            // get the request body
            String reqBody = request.substring(iRequest.getBodyOffset());

            Integer incrementInitialNum = getIncrementedInitialNumber(reqBody);
            if (incrementInitialNum  != null) {
                counter = incrementInitialNum;
                String replaceTargetString = "IncrementItPlease:" + incrementInitialNum

                int offset = reqBody.indexOf(replaceTargetString);
                stdout.println(offset);
                reqBody = reqBody.replaceAll(replaceTargetString, String.valueOf(counter));
                counter++;
                updated = true;
            }

            if (updated) {
                stdout.println("-----Request Before Plugin Update-------");
                stdout.println(helpers.bytesToString(messageInfo.getRequest()));
                stdout.println("-----end output-------");

                byte[] message = helpers.buildHttpMessage(headers, reqBody.getBytes());
                messageInfo.setRequest(message);

                stdout.println("-----Request After Plugin Update-------");
                stdout.println(helpers.bytesToString(messageInfo.getRequest()));
                stdout.println("-----end output-------");
            }
        }
    }
    private Integer getIncrementedInitialNumber(String requestBody) {
        Pattern p = Pattern.compile("(?!IncrementItPlease:)[0-9]+");
        Matcher m = p.matcher(requestBody);
        if(m.find()) {
            String matchStr = m.group(0);
            try {
                return Integer.parseInt(matchStr);
            } catch (NumberFormatException e) {
                stderr.println("Error: " + e);
            }
        }
        return null;
    }
}
