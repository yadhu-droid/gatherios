package com.app.dearly.service.impl;

import com.app.dearly.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:gatherios1@gmail.com}")
    private String fromEmail;

    @Override
    @Async
    public void sendDashboardCredentialsEmail(String toEmail, String generatedPassword, String coupleName, String surpriseId) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            System.err.println("Email recipient is empty. Skipping email dispatch.");
            return;
        }

        if (mailSender == null) {
            System.err.println("JavaMailSender bean not initialized. Skipping email dispatch.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Gatherios Invitations");
            helper.setReplyTo("gatherios1@gmail.com", "Gatherios Support");
            helper.setTo(toEmail.trim());

            String displayCouple = (coupleName != null && !coupleName.trim().isEmpty()) ? coupleName.trim() : "Dear Couple";
            helper.setSubject("Gatherios Dashboard Access Credentials - " + displayCouple);

            message.setHeader("X-Mailer", "Gatherios Mailer 1.0");
            message.setHeader("X-Priority", "3");

            String plainText = buildPlainTextEmail(toEmail.trim(), generatedPassword, displayCouple, surpriseId);
            String htmlBody = buildHtmlEmailContent(toEmail.trim(), generatedPassword, coupleName, surpriseId);

            helper.setText(plainText, htmlBody);

            mailSender.send(message);
            System.out.println("Successfully sent dashboard credentials email to " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send credentials email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildPlainTextEmail(String username, String password, String coupleName, String surpriseId) {
        String dashboardUrl = "https://gatherios.com/dashboard";
        return "Hello " + coupleName + ",\n\n"
                + "Your digital wedding invitation on Gatherios has been generated successfully!\n\n"
                + "Your Dashboard Login Credentials:\n"
                + "----------------------------------------\n"
                + "Username / Email : " + username + "\n"
                + "Password         : " + password + "\n"
                + "Dashboard URL    : " + dashboardUrl + "\n"
                + "----------------------------------------\n\n"
                + "In your dashboard, you can:\n"
                + "- Track guest RSVP attendance in real-time\n"
                + "- Moderate and read guest wishes\n"
                + "- Edit event details and ceremony timings anytime\n\n"
                + "For any support or questions, reach our team directly at 9207146862 / 7306219037 or gatherios1@gmail.com.\n\n"
                + "Warm regards,\n"
                + "The Gatherios Team\n"
                + "https://gatherios.com\n";
    }

    private String buildHtmlEmailContent(String username, String password, String coupleName, String surpriseId) {
        String displayCouple = (coupleName != null && !coupleName.trim().isEmpty()) ? coupleName.trim() : "Dear Couple";
        String dashboardUrl = "https://gatherios.com/dashboard";
        String refId = (surpriseId != null && surpriseId.length() >= 8) ? surpriseId.substring(0, 8).toUpperCase() : "GATH-" + System.currentTimeMillis() % 100000;

        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>Gatherios Dashboard Access</title>"
                + "</head>"
                + "<body style='margin: 0; padding: 0; background-color: #0f0a12; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif; color: #e2d9eb;'>"
                + "<div style='max-width: 600px; margin: 30px auto; background: #1a1024; border-radius: 16px; overflow: hidden; border: 1px solid rgba(212, 175, 55, 0.3); box-shadow: 0 10px 30px rgba(0,0,0,0.5);'>"
                
                // Header Banner
                + "<div style='background: linear-gradient(135deg, #2d0b1e 0%, #4a102a 50%, #1a0826 100%); padding: 36px 24px; text-align: center; border-bottom: 2px solid #d4af37;'>"
                + "<h1 style='margin: 0; font-size: 32px; font-weight: 700; letter-spacing: 2px; color: #f3e5ab; text-transform: uppercase; font-family: Georgia, serif;'>Gatherios</h1>"
                + "<p style='margin: 8px 0 0 0; font-size: 14px; color: #d4af37; letter-spacing: 1px;'>PREMIUM DIGITAL WEDDING INVITATIONS</p>"
                + "</div>"
                
                // Content Body
                + "<div style='padding: 32px 28px;'>"
                + "<h2 style='margin-top: 0; color: #ffffff; font-size: 22px; font-weight: 600;'>Congratulations, " + displayCouple + "! 🥂</h2>"
                + "<p style='font-size: 15px; line-height: 1.6; color: #c4b5d4; margin-bottom: 24px;'>"
                + "Your digital wedding invitation is successfully generated! Below are your exclusive owner login credentials to access your personal dashboard, track guest RSVPs, manage wishes, and update event details anytime."
                + "</p>"
                
                // Credentials Box
                + "<div style='background: rgba(212, 175, 55, 0.08); border: 1px solid rgba(212, 175, 55, 0.4); border-radius: 12px; padding: 24px; margin-bottom: 20px; text-align: center;'>"
                + "<h3 style='margin: 0 0 16px 0; color: #f3e5ab; font-size: 16px; letter-spacing: 1px; text-transform: uppercase;'>🔑 Dashboard Credentials</h3>"
                
                // Username Row
                + "<div style='margin-bottom: 14px; text-align: left; background: rgba(0,0,0,0.25); padding: 12px 16px; border-radius: 8px;'>"
                + "<span style='display: block; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; color: #a593b8; margin-bottom: 4px;'>Username / Email</span>"
                + "<span style='font-size: 16px; font-weight: 600; color: #ffffff; font-family: monospace; word-break: break-all;'>" + username + "</span>"
                + "</div>"
                
                // Password Row
                + "<div style='margin-bottom: 18px; text-align: left; background: rgba(0,0,0,0.25); padding: 12px 16px; border-radius: 8px;'>"
                + "<span style='display: block; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; color: #a593b8; margin-bottom: 4px;'>Generated Password</span>"
                + "<span style='font-size: 22px; font-weight: 700; color: #ffd700; font-family: monospace; letter-spacing: 3px;'>" + password + "</span>"
                + "</div>"
                
                // Dashboard Link Row
                + "<div style='text-align: left; background: rgba(0,0,0,0.25); padding: 12px 16px; border-radius: 8px;'>"
                + "<span style='display: block; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; color: #a593b8; margin-bottom: 4px;'>Dashboard Link</span>"
                + "<a href='" + dashboardUrl + "' target='_blank' style='font-size: 15px; font-weight: 600; color: #64b5f6; text-decoration: underline; font-family: monospace;'>" + dashboardUrl + "</a>"
                + "</div>"
                + "</div>"
                
                // Prominent Support Callout (Right in the main body)
                + "<div style='background: rgba(212, 175, 55, 0.12); border: 1px solid rgba(212, 175, 55, 0.45); border-left: 4px solid #d4af37; padding: 14px 18px; margin-bottom: 24px; border-radius: 8px; font-size: 14px; color: #f3e5ab; line-height: 1.5;'>"
                + "📞 <strong>For support, reach us at:</strong> "
                + "<a href='tel:+919207146862' style='color: #ffffff; font-weight: 700; text-decoration: underline; margin-left: 4px;'>9207146862</a> / "
                + "<a href='tel:+917306219037' style='color: #ffffff; font-weight: 700; text-decoration: underline;'>7306219037</a>"
                + "</div>"
                
                // Primary CTA Button
                + "<div style='text-align: center; margin: 26px 0;'>"
                + "<a href='" + dashboardUrl + "' target='_blank' style='display: inline-block; background: linear-gradient(135deg, #d4af37 0%, #aa7c11 100%); color: #000000; font-weight: 700; font-size: 16px; padding: 14px 36px; border-radius: 50px; text-decoration: none; box-shadow: 0 4px 15px rgba(212, 175, 55, 0.4); text-transform: uppercase; letter-spacing: 1px;'>"
                + "Go to Dashboard →"
                + "</a>"
                + "</div>"
                
                // Features list
                + "<div style='border-top: 1px solid rgba(255,255,255,0.1); padding-top: 18px; margin-top: 18px; font-size: 13px; color: #a593b8; line-height: 1.6;'>"
                + "<p style='margin: 0 0 6px 0; font-weight: 600; color: #d4af37;'>What you can do in your Dashboard:</p>"
                + "<ul style='margin: 0; padding-left: 18px;'>"
                + "<li>Track real-time guest RSVP responses & attendance counts</li>"
                + "<li>View and moderate guest wishes & blessings</li>"
                + "<li>Edit event details, venues, and ceremony timings anytime</li>"
                + "</ul>"
                + "</div>"
                
                + "</div>"
                
                // Footer
                + "<div style='background: #120a1a; padding: 22px; text-align: center; border-top: 1px solid rgba(255,255,255,0.05); font-size: 12px; color: #7c6890;'>"
                + "<p style='margin: 0 0 6px 0;'>For support, reach us at <a href='tel:+919207146862' style='color: #d4af37; text-decoration: none; font-weight: 600;'>9207146862</a> / <a href='tel:+917306219037' style='color: #d4af37; text-decoration: none; font-weight: 600;'>7306219037</a> or <a href='mailto:gatherios1@gmail.com' style='color: #d4af37; text-decoration: none;'>gatherios1@gmail.com</a></p>"
                + "<p style='margin: 0; font-size: 11px; color: #5a4b6b;'>Ref: " + refId + " · © " + java.time.Year.now().getValue() + " Gatherios. All rights reserved.</p>"
                + "</div>"
                
                + "</div>"
                + "</body>"
                + "</html>";
    }
}
