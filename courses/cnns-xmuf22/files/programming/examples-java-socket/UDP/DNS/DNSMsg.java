public class DNSMsg {
    private short   id;

    private boolean qrFlag; // query or response flag
    private byte    opCode; // op code
    private boolean AAFlag; // Authoritative answer flag 
    private boolean TCFlag; // Truncation flag
    private boolean RDFlag; // Recursion desired
    private boolean RAFlag; // Recursion available
    
    private short   response;       // response code

    private short   questionCount;
    private short   answerCount; 
    private short   authorityCount;
    private short   additionalCount;

    private Question[] questions;
    // define other records

    public DNSMsg(boolean id)
        this.id = id;
    }

    public void setQrFlag(boolean flag) {
        this.qrFlag = flag;
    }

    public void setQuestions(Question[] questions) {
        this.questions = questions;
        this.questionCount = questions.length;
    }

    @Override
    public String toString() {
        return "DNSMsg{" +
                "isQuery=" + qrFlag +
                ", opCode=" + opCode +
                '}';
    }
}