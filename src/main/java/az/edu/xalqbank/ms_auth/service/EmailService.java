package az.edu.xalqbank.ms_auth.service;


public interface EmailService {
    void sendEmail(String to, String subject, String text);

}
