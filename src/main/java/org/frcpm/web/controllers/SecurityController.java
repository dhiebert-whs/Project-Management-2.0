// src/main/java/org/frcpm/web/controllers/SecurityController.java

package org.frcpm.web.controllers;

import org.frcpm.security.MFAService;
import org.frcpm.security.UserPrincipal;
import org.frcpm.services.COPPAComplianceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for security-related operations including MFA and COPPA compliance.
 * 
 * ✅ FIXED: Removed conflicting /login mapping - handled by AuthController
 */
@Controller
public class SecurityController extends BaseController {
    
    @Autowired
    private MFAService mfaService;
    
    @Autowired
    private COPPAComplianceService coppaService;
    
    // =========================================================================
    // ✅ REMOVED: /login mapping (now handled by AuthController)
    // =========================================================================
    
    // =========================================================================
    // MFA SETUP AND VERIFICATION
    // =========================================================================
    
    /**
     * Display MFA setup page.
     */
    @GetMapping("/mfa/setup")
    public String mfaSetupPage(@AuthenticationPrincipal UserPrincipal user, Model model) {
        
        if (!user.requiresMFA()) {
            addErrorMessage(model, "Multi-factor authentication is not required for your role.");
            return redirect("/dashboard");
        }
        
        if (user.isMFAEnabled()) {
            addInfoMessage(model, "Multi-factor authentication is already enabled for your account.");
            return redirect("/dashboard");
        }
        
        try {
            MFAService.MFASetupData setupData = mfaService.initiateMFASetup(user.getUser().getId());
            
            model.addAttribute("secret", setupData.getSecret());
            model.addAttribute("formattedSecret", setupData.getFormattedSecret());
            model.addAttribute("qrCodeUrl", setupData.getQrCodeUrl());
            model.addAttribute("issuer", setupData.getIssuer());
            model.addAttribute("username", user.getUsername());
            
            addBreadcrumbs(model, "Security", "/profile/security", "Setup MFA", "/mfa/setup");
            
            return view("auth/mfa-setup");
            
        } catch (Exception e) {
            LOGGER.severe("Error initiating MFA setup: " + e.getMessage());
            addErrorMessage(model, "Unable to initiate MFA setup. Please try again or contact support.");
            return redirect("/dashboard");
        }
    }
    
    /**
     * Process MFA setup completion.
     */
    @PostMapping("/mfa/setup")
    public String completeMfaSetup(@AuthenticationPrincipal UserPrincipal user,
                                  @RequestParam String secret,
                                  @RequestParam String verificationCode,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        
        try {
            boolean success = mfaService.completeMFASetup(user.getUser().getId(), secret, verificationCode);
            
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Multi-factor authentication has been successfully enabled for your account.");
                return redirect("/dashboard");
            } else {
                model.addAttribute("secret", secret);
                model.addAttribute("formattedSecret", formatSecretForDisplay(secret));
                addErrorMessage(model, "Invalid verification code. Please try again.");
                return view("auth/mfa-setup");
            }
            
        } catch (Exception e) {
            LOGGER.severe("Error completing MFA setup: " + e.getMessage());
            addErrorMessage(model, "Unable to complete MFA setup. Please try again.");
            return view("auth/mfa-setup");
        }
    }
    
    /**
     * Display MFA verification page.
     */
    @GetMapping("/mfa/verify")
    public String mfaVerifyPage(@AuthenticationPrincipal UserPrincipal user, Model model) {
        
        if (!user.requiresMFA() || !user.isMFAEnabled()) {
            return redirect("/dashboard");
        }
        
        // Check if already verified in this session
        // This would be tracked in session attributes in a full implementation
        
        model.addAttribute("timeRemaining", mfaService.getTokenTimeRemaining());
        addBreadcrumbs(model, "Security Verification", "/mfa/verify");
        
        return view("auth/mfa-verify");
    }
    
    /**
     * Process MFA verification.
     */
    @PostMapping("/mfa/verify")
    public String verifyMfa(@AuthenticationPrincipal UserPrincipal user,
                           @RequestParam String token,
                           Model model,
                           jakarta.servlet.http.HttpServletRequest request,
                           RedirectAttributes redirectAttributes) {
        
        try {
            boolean isValid = mfaService.validateMFAToken(user.getUser().getId(), token);
            
            if (isValid) {
                // Mark MFA as verified in this session
                request.getSession().setAttribute("mfaVerified", true);
                
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Multi-factor authentication verified successfully.");
                return redirect("/dashboard");
            } else {
                addErrorMessage(model, "Invalid authentication code. Please try again.");
                model.addAttribute("timeRemaining", mfaService.getTokenTimeRemaining());
                return view("auth/mfa-verify");
            }
            
        } catch (Exception e) {
            LOGGER.severe("Error verifying MFA: " + e.getMessage());
            addErrorMessage(model, "Unable to verify authentication code. Please try again.");
            return view("auth/mfa-verify");
        }
    }
    
    // =========================================================================
    // COPPA COMPLIANCE PAGES
    // =========================================================================
    
    /**
     * Display COPPA consent required page.
     */
    @GetMapping("/coppa/consent-required")
    public String coppaConsentRequired(@AuthenticationPrincipal UserPrincipal user, Model model) {
        
        if (!user.requiresCOPPACompliance()) {
            return redirect("/dashboard");
        }
        
        if (user.hasParentalConsent()) {
            return redirect("/dashboard");
        }
        
        model.addAttribute("user", user.getUser());
        model.addAttribute("parentEmail", user.getUser().getParentEmail());
        
        return view("coppa/consent-required");
    }
    
    /**
     * Process parental consent from email link.
     */
    @GetMapping("/coppa/consent")
    public String processParentalConsent(@RequestParam String token,
                                        @RequestParam boolean consent,
                                        Model model) {
        
        try {
            boolean processed = coppaService.processParentalConsent(token, consent);
            
            if (processed) {
                if (consent) {
                    addSuccessMessage(model, 
                        "Parental consent has been granted. The student account is now active.");
                } else {
                    addInfoMessage(model, 
                        "Parental consent was not granted. The student account remains inactive.");
                }
            } else {
                addErrorMessage(model, 
                    "Invalid or expired consent link. Please contact the team for assistance.");
            }
            
        } catch (Exception e) {
            LOGGER.severe("Error processing parental consent: " + e.getMessage());
            addErrorMessage(model, "An error occurred processing the consent. Please try again.");
        }
        
        return view("coppa/consent-result");
    }
    
    // =========================================================================
    // HELPER METHODS
    // =========================================================================
    
    /**
     * Formats TOTP secret for display with spaces every 4 characters.
     */
    private String formatSecretForDisplay(String secret) {
        if (secret == null || secret.isEmpty()) {
            return "";
        }
        
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < secret.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(secret.charAt(i));
        }
        return formatted.toString();
    }
}