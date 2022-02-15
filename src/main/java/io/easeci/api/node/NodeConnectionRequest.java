package io.easeci.api.node;

import io.easeci.api.validation.ValidationError;
import io.easeci.api.validation.Validator;
import io.easeci.server.TransferProtocol;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

import static io.easeci.api.validation.CommonValidatorSet.combine;
import static io.easeci.api.validation.CommonValidatorSet.nullCheck;

@Data
public class NodeConnectionRequest implements Validator {
    private String nodeIp;
    private String nodeName;
    private String nodePort;
    private String domainName;
    private TransferProtocol transferProtocol;
    private String connectionToken;

    @Override
    public List<ValidationError> validate() {
        return combine(
                Arrays.asList(
                        nullCheck(this.nodeIp, "nodeIp"),
                        nullCheck(this.nodeName, "nodeName"),
                        nullCheck(this.nodePort, "nodePort"),
                        nullCheck(this.domainName, "domainName"),
                        nullCheck(this.transferProtocol, "transferProtocol"),
                        nullCheck(this.connectionToken, "connectionToken")
                ));
    }
}
