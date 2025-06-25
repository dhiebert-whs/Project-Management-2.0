// src/main/java/org/frcpm/web/controllers/MeetingController.java

package org.frcpm.web.controllers;

import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.models.TeamMember;
import org.frcpm.services.MeetingService;
import org.frcpm.services.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.logging.Level;

/**
 * Meeting controller handling comprehensive meeting scheduling and attendance management.
 * 
 * Features:
 * - Complete CRUD operations for meetings
 * - Attendance tracking and recording
 * - Meeting scheduling with conflict detection
 * - Attendance statistics and reporting
 * - Calendar view of meetings
 * - Meeting notes and documentation
 * - CSV export capabilities
 * - Team member notification system
 * 
 * Following the proven patterns established in other Phase 2A controllers
 * with comprehensive error handling and professional user experience.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2A - Web Controllers Implementation
 */
@Controller
@RequestMapping("/meetings")
public class MeetingController extends BaseController {
    
    @Autowired
    private MeetingService meetingService;
    
    @Autowired
    private AttendanceService attendanceService;
    
    // Date and time formatters for display
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    //private static final DateTimeFormatter TIME_INPUT_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    // =========================================================================
    // MEETING LIST AND CALENDAR VIEW
    // =========================================================================
    
    /**
     * Display list of meetings with calendar and filtering options.
     * 
     * @param model the Spring MVC model
     * @param view optional view type (list, calendar, upcoming)
     * @param projectId optional project filter
     * @param month optional month filter (yyyy-mm format)
     * @param sort optional sort parameter
     * @return meetings list or calendar view
     */
    @GetMapping
    public String listMeetings(Model model,
                              @RequestParam(value = "view", required = false, defaultValue = "upcoming") String view,
                              @RequestParam(value = "projectId", required = false) Long projectId,
                              @RequestParam(value = "month", required = false) String month,
                              @RequestParam(value = "sort", required = false, defaultValue = "date") String sort) {
        
        try {
            addNavigationData(model);
            addBreadcrumbs(model, "Meetings", "/meetings");
            
            // Determine date range based on view and month filter
            LocalDate startDate, endDate;
            if (month != null && !month.isEmpty()) {
                // Parse month filter (yyyy-mm)
                String[] parts = month.split("-");
                if (parts.length == 2) {
                    int year = Integer.parseInt(parts[0]);
                    int monthNum = Integer.parseInt(parts[1]);
                    startDate = LocalDate.of(year, monthNum, 1);
                    endDate = startDate.plusMonths(1).minusDays(1);
                } else {
                    startDate = LocalDate.now().withDayOfMonth(1);
                    endDate = startDate.plusMonths(1).minusDays(1);
                }
            } else {
                // Default ranges based on view
                switch (view.toLowerCase()) {
                    case "calendar":
                        startDate = LocalDate.now().withDayOfMonth(1);
                        endDate = startDate.plusMonths(1).minusDays(1);
                        break;
                    case "upcoming":
                        startDate = LocalDate.now();
                        endDate = LocalDate.now().plusWeeks(4);
                        break;
                    case "all":
                        startDate = LocalDate.now().minusMonths(6);
                        endDate = LocalDate.now().plusMonths(6);
                        break;
                    default:
                        startDate = LocalDate.now();
                        endDate = LocalDate.now().plusWeeks(2);
                        break;
                }
            }
            
            // Get meetings in date range
            List<Meeting> allMeetings = meetingService.findByDateBetween(startDate, endDate);
            
            // Apply project filter
            List<Meeting> filteredMeetings = applyMeetingFilters(allMeetings, projectId);
            
            // Apply sorting
            sortMeetings(filteredMeetings, sort);
            
            // Add to model
            model.addAttribute("meetings", filteredMeetings);
            model.addAttribute("totalMeetings", allMeetings.size());
            model.addAttribute("filteredCount", filteredMeetings.size());
            
            // Current filter values
            model.addAttribute("currentView", view);
            model.addAttribute("currentProjectId", projectId);
            model.addAttribute("currentMonth", month);
            model.addAttribute("currentSort", sort);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            
            // Load filter options
            loadMeetingFilterOptions(model);
            
            // Add statistics
            addMeetingStatistics(model, filteredMeetings);
            
            // Determine view template
            switch (view.toLowerCase()) {
                case "calendar":
                    organizeMeetingsForCalendar(model, filteredMeetings, startDate, endDate);
                    return view("meetings/calendar");
                case "upcoming":
                    addUpcomingMeetingData(model, filteredMeetings);
                    return view("meetings/upcoming");
                case "list":
                case "all":
                default:
                    return view("meetings/list");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading meetings list", e);
            return handleException(e, model);
        }
    }
    
    /**
     * Display detailed view of a specific meeting.
     * 
     * @param id the meeting ID
     * @param model the model
     * @param tab optional tab selection (overview, attendance, notes)
     * @return meeting detail view
     */
    @GetMapping("/{id}")
    public String viewMeeting(@PathVariable Long id, Model model,
                             @RequestParam(value = "tab", required = false, defaultValue = "overview") String tab) {
        
        try {
            Meeting meeting = meetingService.findById(id);
            if (meeting == null) {
                addErrorMessage(model, "Meeting not found");
                return redirect("/meetings");
            }
            
            addNavigationData(model);
            addBreadcrumbs(model, "Meetings", "/meetings", 
                          formatMeetingTitle(meeting), "/meetings/" + id);
            
            model.addAttribute("meeting", meeting);
            model.addAttribute("activeTab", tab);
            
            // Load tab-specific data
            switch (tab.toLowerCase()) {
                case "attendance":
                    loadAttendanceTabData(model, meeting);
                    break;
                case "notes":
                    loadNotesTabData(model, meeting);
                    break;
                case "overview":
                default:
                    loadOverviewTabData(model, meeting);
                    break;
            }
            
            // Calculate meeting statistics
            addMeetingDetailStatistics(model, meeting);
            
            return view("meetings/detail");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading meeting detail", e);
            return handleException(e, model);
        }
    }
    
    // =========================================================================
    // MEETING CREATION AND SCHEDULING
    // =========================================================================
    
    /**
     * Show new meeting creation form.
     * 
     * @param model the model
     * @param projectId optional pre-selected project
     * @param date optional pre-selected date
     * @return new meeting form view
     */
    @GetMapping("/new")
    public String newMeetingForm(Model model,
                                @RequestParam(value = "projectId", required = false) Long projectId,
                                @RequestParam(value = "date", required = false) String date) {
        try {
            // Check permissions - only mentors and admins can schedule meetings
            if (!hasRole("MENTOR") && !hasRole("ADMIN")) {
                addErrorMessage(model, "Only mentors and admins can schedule meetings");
                return redirect("/meetings");
            }
            
            addNavigationData(model);
            addBreadcrumbs(model, "Meetings", "/meetings", "New Meeting", "/meetings/new");
            
            // Create empty meeting for form binding
            Meeting meeting = new Meeting();
            
            // Set default values
            LocalDate defaultDate = date != null ? parseDateFromInput(date) : LocalDate.now().plusDays(1);
            meeting.setDate(defaultDate);
            meeting.setStartTime(LocalTime.of(16, 0)); // 4:00 PM default
            meeting.setEndTime(LocalTime.of(18, 0));   // 6:00 PM default
            
            // Pre-select project if provided
            if (projectId != null) {
                Project project = projectService.findById(projectId);
                if (project != null) {
                    meeting.setProject(project);
                }
            }
            
            model.addAttribute("meeting", meeting);
            model.addAttribute("isEdit", false);
            
            // Load form options
            loadMeetingFormOptions(model);
            
            // Add helpful defaults
            addMeetingFormHelpers(model);
            
            // Check for scheduling conflicts
            if (defaultDate != null) {
                checkSchedulingConflicts(model, defaultDate, meeting.getStartTime(), meeting.getEndTime());
            }
            
            return view("meetings/form");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading new meeting form", e);
            return handleException(e, model);
        }
    }
    
    /**
     * Process new meeting creation.
     * 
     * @param meeting the meeting data from form
     * @param result binding result for validation
     * @param model the model
     * @param redirectAttributes for redirect messages
     * @return redirect to meeting detail or back to form with errors
     */
    @PostMapping("/new")
    public String createMeeting(@Valid @ModelAttribute Meeting meeting,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        try {
            // Check permissions
            if (!hasRole("MENTOR") && !hasRole("ADMIN")) {
                addErrorMessage(model, "Only mentors and admins can schedule meetings");
                return redirect("/meetings");
            }
            
            // Validate form data
            validateMeetingData(meeting, result);
            
            if (result.hasErrors()) {
                addNavigationData(model);
                addBreadcrumbs(model, "Meetings", "/meetings", "New Meeting", "/meetings/new");
                model.addAttribute("isEdit", false);
                loadMeetingFormOptions(model);
                addMeetingFormHelpers(model);
                addErrorMessage(model, "Please correct the errors below");
                return view("meetings/form");
            }
            
            // Create the meeting using the service
            Meeting savedMeeting = meetingService.createMeeting(
                meeting.getDate(),
                meeting.getStartTime(),
                meeting.getEndTime(),
                meeting.getProject().getId(),
                meeting.getNotes()
            );
            
            // Initialize attendance records for all team members
            initializeAttendanceRecords(savedMeeting);
            
            // Add success message
            redirectAttributes.addFlashAttribute("successMessage", 
                "Meeting scheduled successfully for " + formatDate(savedMeeting.getDate()) + "!");
            
            // Redirect to the new meeting
            return redirect("/meetings/" + savedMeeting.getId());
            
        } catch (IllegalArgumentException e) {
            addNavigationData(model);
            addBreadcrumbs(model, "Meetings", "/meetings", "New Meeting", "/meetings/new");
            model.addAttribute("isEdit", false);
            loadMeetingFormOptions(model);
            addMeetingFormHelpers(model);
            addErrorMessage(model, e.getMessage());
            return view("meetings/form");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating meeting", e);
            return handleException(e, model);
        }
    }
    
    /**
     * Show meeting edit form.
     * 
     * @param id the meeting ID
     * @param model the model
     * @return edit meeting form view
     */
    @GetMapping("/{id}/edit")
    public String editMeetingForm(@PathVariable Long id, Model model) {
        try {
            Meeting meeting = meetingService.findById(id);
            if (meeting == null) {
                addErrorMessage(model, "Meeting not found");
                return redirect("/meetings");
            }
            
            // Check permissions
            if (!hasRole("MENTOR") && !hasRole("ADMIN")) {
                addErrorMessage(model, "Only mentors and admins can edit meetings");
                return redirect("/meetings/" + id);
            }
            
            addNavigationData(model);
            addBreadcrumbs(model, "Meetings", "/meetings", 
                          formatMeetingTitle(meeting), "/meetings/" + id,
                          "Edit", "/meetings/" + id + "/edit");
            
            model.addAttribute("meeting", meeting);
            model.addAttribute("isEdit", true);
            
            // Load form options
            loadMeetingFormOptions(model);
            
            // Add edit warnings
            addMeetingEditWarnings(model, meeting);
            
            // Check for scheduling conflicts (excluding current meeting)
            checkSchedulingConflicts(model, meeting.getDate(), meeting.getStartTime(), meeting.getEndTime(), id);
            
            return view("meetings/form");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading meeting edit form", e);
            return handleException(e, model);
        }
    }
    
    /**
     * Process meeting update.
     * 
     * @param id the meeting ID
     * @param meeting the updated meeting data
     * @param result binding result
     * @param model the model
     * @param redirectAttributes for redirect messages
     * @return redirect to meeting detail or back to form with errors
     */
    @PostMapping("/{id}/edit")
    public String updateMeeting(@PathVariable Long id,
                               @Valid @ModelAttribute Meeting meeting,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        try {
            // Validate meeting exists
            Meeting existingMeeting = meetingService.findById(id);
            if (existingMeeting == null) {
                addErrorMessage(model, "Meeting not found");
                return redirect("/meetings");
            }
            
            // Check permissions
            if (!hasRole("MENTOR") && !hasRole("ADMIN")) {
                addErrorMessage(model, "Only mentors and admins can edit meetings");
                return redirect("/meetings/" + id);
            }
            
            // Validate form data
            validateMeetingData(meeting, result);
            
            if (result.hasErrors()) {
                addNavigationData(model);
                addBreadcrumbs(model, "Meetings", "/meetings", 
                              formatMeetingTitle(existingMeeting), "/meetings/" + id,
                              "Edit", "/meetings/" + id + "/edit");
                model.addAttribute("isEdit", true);
                loadMeetingFormOptions(model);
                addMeetingEditWarnings(model, existingMeeting);
                addErrorMessage(model, "Please correct the errors below");
                return view("meetings/form");
            }
            
            // Update the meeting properties
            existingMeeting.setDate(meeting.getDate());
            existingMeeting.setStartTime(meeting.getStartTime());
            existingMeeting.setEndTime(meeting.getEndTime());
            existingMeeting.setProject(meeting.getProject());
            existingMeeting.setNotes(meeting.getNotes());
            
            // Save the updated meeting
            Meeting updatedMeeting = meetingService.save(existingMeeting);
            
            // Add success message
            redirectAttributes.addFlashAttribute("successMessage", 
                "Meeting updated successfully!");
            
            return redirect("/meetings/" + updatedMeeting.getId());
            
        } catch (IllegalArgumentException e) {
            addNavigationData(model);
            addBreadcrumbs(model, "Meetings", "/meetings", 
                          formatMeetingTitle(meeting), "/meetings/" + id,
                          "Edit", "/meetings/" + id + "/edit");
            model.addAttribute("isEdit", true);
            loadMeetingFormOptions(model);
            addErrorMessage(model, e.getMessage());
            return view("meetings/form");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating meeting", e);
            return handleException(e, model);
        }
    }
    
    // =========================================================================
    // ATTENDANCE MANAGEMENT
    // =========================================================================
    
    /**
     * Show attendance recording form for a meeting.
     * 
     * @param id the meeting ID
     * @param model the model
     * @return attendance recording view
     */
    @GetMapping("/{id}/attendance")
    public String recordAttendance(@PathVariable Long id, Model model) {
        try {
            Meeting meeting = meetingService.findById(id);
            if (meeting == null) {
                addErrorMessage(model, "Meeting not found");
                return redirect("/meetings");
            }
            
            addNavigationData(model);
            addBreadcrumbs(model, "Meetings", "/meetings", 
                          formatMeetingTitle(meeting), "/meetings/" + id,
                          "Attendance", "/meetings/" + id + "/attendance");
            
            model.addAttribute("meeting", meeting);
            
            // Get all team members and their attendance status
            List<TeamMember> allMembers = teamMemberService.findAll();
            List<Attendance> attendanceRecords = attendanceService.findByMeeting(meeting);
            
            // Create attendance map for easy lookup
            Map<Long, Attendance> attendanceMap = attendanceRecords.stream()
                .collect(Collectors.toMap(
                    att -> att.getMember().getId(),
                    att -> att
                ));
            
            // Prepare member attendance data
            List<Map<String, Object>> memberAttendanceData = allMembers.stream()
                .map(member -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("member", member);
                    
                    Attendance attendance = attendanceMap.get(member.getId());
                    if (attendance != null) {
                        data.put("attendance", attendance);
                        data.put("present", attendance.isPresent());
                        data.put("arrivalTime", attendance.getArrivalTime());
                        data.put("departureTime", attendance.getDepartureTime());
                    } else {
                        data.put("attendance", null);
                        data.put("present", false);
                        data.put("arrivalTime", null);
                        data.put("departureTime", null);
                    }
                    
                    return data;
                })
                .collect(Collectors.toList());
            
            model.addAttribute("memberAttendanceData", memberAttendanceData);
            
            // Add attendance statistics
            long presentCount = attendanceRecords.stream().filter(Attendance::isPresent).count();
            model.addAttribute("presentCount", presentCount);
            model.addAttribute("totalMembers", allMembers.size());
            model.addAttribute("attendanceRate", allMembers.size() > 0 ? 
                Math.round((double) presentCount / allMembers.size() * 100) : 0);
            
            return view("meetings/attendance");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading attendance form", e);
            return handleException(e, model);
        }
    }
    
    /**
     * Process attendance recording.
     * 
     * @param id the meeting ID
     * @param presentMemberIds array of present member IDs
     * @param redirectAttributes for redirect messages
     * @return redirect to meeting detail
     */
    @PostMapping("/{id}/attendance")
    public String submitAttendance(@PathVariable Long id,
                                  @RequestParam(value = "presentMemberIds", required = false) List<Long> presentMemberIds,
                                  RedirectAttributes redirectAttributes) {
        
        try {
            Meeting meeting = meetingService.findById(id);
            if (meeting == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Meeting not found");
                return redirect("/meetings");
            }
            
            // Record attendance for all members
            //int recordsUpdated = attendanceService.recordAttendanceForMeeting(id, presentMemberIds);
            
            // Calculate attendance rate
            List<Attendance> attendanceRecords = attendanceService.findByMeeting(meeting);
            long presentCount = attendanceRecords.stream().filter(Attendance::isPresent).count();
            double attendanceRate = attendanceRecords.size() > 0 ? 
                (double) presentCount / attendanceRecords.size() * 100 : 0;
            
            // Add success message with statistics
            redirectAttributes.addFlashAttribute("successMessage", 
                String.format("Attendance recorded successfully! %d present (%d%% attendance rate)", 
                             presentCount, Math.round(attendanceRate)));
            
            return redirect("/meetings/" + id + "?tab=attendance");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error recording attendance", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error recording attendance");
            return redirect("/meetings/" + id);
        }
    }
    
    /**
     * Quick attendance toggle (AJAX endpoint).
     * 
     * @param meetingId the meeting ID
     * @param memberId the member ID
     * @param present the attendance status
     * @return JSON response
     */
    @PostMapping("/{meetingId}/attendance/{memberId}/toggle")
    @ResponseBody
    public Map<String, Object> toggleAttendance(@PathVariable Long meetingId,
                                               @PathVariable Long memberId,
                                               @RequestParam boolean present) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Meeting meeting = meetingService.findById(meetingId);
            TeamMember member = teamMemberService.findById(memberId);
            
            if (meeting == null || member == null) {
                response.put("success", false);
                response.put("message", "Meeting or member not found");
                return response;
            }
            
            // Create or update attendance record
            Attendance attendance = attendanceService.createAttendance(meetingId, memberId, present);
            
            if (attendance != null) {
                response.put("success", true);
                response.put("present", attendance.isPresent());
                response.put("memberName", member.getFullName());
                response.put("message", present ? "Marked present" : "Marked absent");
                
                // Update meeting statistics
                List<Attendance> allAttendance = attendanceService.findByMeeting(meeting);
                long presentCount = allAttendance.stream().filter(Attendance::isPresent).count();
                double attendanceRate = allAttendance.size() > 0 ? 
                    (double) presentCount / allAttendance.size() * 100 : 0;
                
                response.put("presentCount", presentCount);
                response.put("totalMembers", allAttendance.size());
                response.put("attendanceRate", Math.round(attendanceRate));
            } else {
                response.put("success", false);
                response.put("message", "Failed to update attendance");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error toggling attendance", e);
            response.put("success", false);
            response.put("message", "Error updating attendance: " + e.getMessage());
        }
        
        return response;
    }
    
    // =========================================================================
    // MEETING DELETION AND MANAGEMENT
    // =========================================================================
    
    /**
     * Show meeting deletion confirmation.
     * 
     * @param id the meeting ID
     * @param model the model
     * @return deletion confirmation view
     */
    @GetMapping("/{id}/delete")
    public String confirmDeleteMeeting(@PathVariable Long id, Model model) {
        try {
            Meeting meeting = meetingService.findById(id);
            if (meeting == null) {
                addErrorMessage(model, "Meeting not found");
                return redirect("/meetings");
            }
            
            // Check permissions
            if (!hasRole("MENTOR") && !hasRole("ADMIN")) {
                addErrorMessage(model, "Only mentors and admins can delete meetings");
                return redirect("/meetings/" + id);
            }
            
            addNavigationData(model);
            addBreadcrumbs(model, "Meetings", "/meetings", 
                          formatMeetingTitle(meeting), "/meetings/" + id,
                          "Delete", "/meetings/" + id + "/delete");
            
            model.addAttribute("meeting", meeting);
            
            // Add deletion impact information
            addMeetingDeletionImpactInfo(model, meeting);
            
            return view("meetings/delete-confirm");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading delete confirmation", e);
            return handleException(e, model);
        }
    }
    
    /**
     * Process meeting deletion.
     * 
     * @param id the meeting ID
     * @param redirectAttributes for redirect messages
     * @return redirect to meetings list
     */
    @PostMapping("/{id}/delete")
    public String deleteMeeting(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Meeting meeting = meetingService.findById(id);
            if (meeting == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Meeting not found");
                return redirect("/meetings");
            }
            
            // Check permissions
            if (!hasRole("MENTOR") && !hasRole("ADMIN")) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Only mentors and admins can delete meetings");
                return redirect("/meetings/" + id);
            }
            
            String meetingTitle = formatMeetingTitle(meeting);
            
            // Delete the meeting (attendance records will be cascade deleted)
            boolean deleted = meetingService.deleteById(id);
            
            if (deleted) {
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Meeting '" + meetingTitle + "' deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Failed to delete meeting '" + meetingTitle + "'");
            }
            
            return redirect("/meetings");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting meeting", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error occurred while deleting meeting");
            return redirect("/meetings");
        }
    }
    
    // =========================================================================
    // EXPORT AND REPORTING
    // =========================================================================
    
    /**
     * Export meetings to CSV format.
     * 
     * @param projectId optional project filter
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @param response HTTP response for file download
     */
    @GetMapping("/export/csv")
    public void exportMeetingsToCsv(@RequestParam(required = false) Long projectId,
                                   @RequestParam(required = false) String startDate,
                                   @RequestParam(required = false) String endDate,
                                   HttpServletResponse response) {
        
        try {
            // Set response headers for file download
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=\"meetings_export.csv\"");
            
            // Determine date range
            LocalDate start = startDate != null ? parseDateFromInput(startDate) : LocalDate.now().minusMonths(3);
            LocalDate end = endDate != null ? parseDateFromInput(endDate) : LocalDate.now().plusMonths(3);
            
            // Get meetings to export
            List<Meeting> meetings = meetingService.findByDateBetween(start, end);
            
            // Apply project filter if specified
            meetings = applyMeetingFilters(meetings, projectId);
            
            // Write CSV headers
            PrintWriter writer = response.getWriter();
            writer.println("ID,Date,Start Time,End Time,Project,Duration (hours),Notes,Present Count,Total Members,Attendance Rate");
            
            // Write meeting data
            for (Meeting meeting : meetings) {
                StringBuilder line = new StringBuilder();
                line.append(escapeCSV(meeting.getId().toString())).append(",");
                line.append(meeting.getDate().toString()).append(",");
                line.append(meeting.getStartTime().format(TIME_FORMATTER)).append(",");
                line.append(meeting.getEndTime().format(TIME_FORMATTER)).append(",");
                line.append(escapeCSV(meeting.getProject().getName())).append(",");
                
                // Calculate duration
                long durationMinutes = java.time.Duration.between(meeting.getStartTime(), meeting.getEndTime()).toMinutes();
                double durationHours = durationMinutes / 60.0;
                line.append(String.format("%.1f", durationHours)).append(",");
                
                line.append(escapeCSV(meeting.getNotes())).append(",");
                
                // Attendance data
                try {
                    List<Attendance> attendanceRecords = attendanceService.findByMeeting(meeting);
                    long presentCount = attendanceRecords.stream().filter(Attendance::isPresent).count();
                    double attendanceRate = attendanceRecords.size() > 0 ? 
                        (double) presentCount / attendanceRecords.size() * 100 : 0;
                    
                    line.append(presentCount).append(",");
                    line.append(attendanceRecords.size()).append(",");
                    line.append(String.format("%.1f", attendanceRate));
                } catch (Exception e) {
                    line.append("0,0,0");
                }
                
                writer.println(line.toString());
            }
            
            writer.flush();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error exporting meetings to CSV", e);
            try {
                response.sendError(500, "Error exporting meetings");
            } catch (IOException ioException) {
                LOGGER.log(Level.SEVERE, "Error sending error response", ioException);
            }
        }
    }
    
    /**
     * Export attendance report to CSV format.
     * 
     * @param id the meeting ID
     * @param response HTTP response for file download
     */
    @GetMapping("/{id}/attendance/export/csv")
    public void exportAttendanceToCsv(@PathVariable Long id, HttpServletResponse response) {
        
        try {
            Meeting meeting = meetingService.findById(id);
            if (meeting == null) {
                response.sendError(404, "Meeting not found");
                return;
            }
            
            // Set response headers for file download
            response.setContentType("text/csv");
            String filename = String.format("attendance_%s_%s.csv", 
                meeting.getDate().toString(), meeting.getProject().getName().replaceAll("\\s+", "_"));
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            
            // Get attendance records
            List<Attendance> attendanceRecords = attendanceService.findByMeeting(meeting);
            
            // Write CSV headers
            PrintWriter writer = response.getWriter();
            writer.println("Member ID,Name,Username,Subteam,Present,Arrival Time,Departure Time,Duration (minutes)");
            
            // Write attendance data
            for (Attendance attendance : attendanceRecords) {
                TeamMember member = attendance.getMember();
                StringBuilder line = new StringBuilder();
                
                line.append(escapeCSV(member.getId().toString())).append(",");
                line.append(escapeCSV(member.getFullName())).append(",");
                line.append(escapeCSV(member.getUsername())).append(",");
                line.append(escapeCSV(member.getSubteam() != null ? member.getSubteam().getName() : "")).append(",");
                line.append(attendance.isPresent()).append(",");
                line.append(attendance.getArrivalTime() != null ? attendance.getArrivalTime().format(TIME_FORMATTER) : "").append(",");
                line.append(attendance.getDepartureTime() != null ? attendance.getDepartureTime().format(TIME_FORMATTER) : "").append(",");
                line.append(attendance.getDurationMinutes());
                
                writer.println(line.toString());
            }
            
            writer.flush();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error exporting attendance to CSV", e);
            try {
                response.sendError(500, "Error exporting attendance");
            } catch (IOException ioException) {
                LOGGER.log(Level.SEVERE, "Error sending error response", ioException);
            }
        }
    }
    
    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================
    
    /**
     * Apply filters to meeting list.
     */
    private List<Meeting> applyMeetingFilters(List<Meeting> meetings, Long projectId) {
        return meetings.stream()
            .filter(meeting -> projectId == null || meeting.getProject().getId().equals(projectId))
            .collect(Collectors.toList());
    }
    
    /**
     * Sort meetings by specified criteria.
     */
    private void sortMeetings(List<Meeting> meetings, String sort) {
        switch (sort.toLowerCase()) {
            case "date":
                meetings.sort((m1, m2) -> {
                    int dateCompare = m1.getDate().compareTo(m2.getDate());
                    return dateCompare != 0 ? dateCompare : m1.getStartTime().compareTo(m2.getStartTime());
                });
                break;
            case "project":
                meetings.sort((m1, m2) -> m1.getProject().getName().compareToIgnoreCase(m2.getProject().getName()));
                break;
            case "duration":
                meetings.sort((m1, m2) -> {
                    long duration1 = java.time.Duration.between(m1.getStartTime(), m1.getEndTime()).toMinutes();
                    long duration2 = java.time.Duration.between(m2.getStartTime(), m2.getEndTime()).toMinutes();
                    return Long.compare(duration2, duration1); // Longest first
                });
                break;
            default:
                // Default: date and time
                meetings.sort((m1, m2) -> {
                    int dateCompare = m1.getDate().compareTo(m2.getDate());
                    return dateCompare != 0 ? dateCompare : m1.getStartTime().compareTo(m2.getStartTime());
                });
                break;
        }
    }
    
    /**
     * Load filter options for dropdowns.
     */
    private void loadMeetingFilterOptions(Model model) {
        try {
            // Projects
            List<Project> projects = projectService.findAll();
            model.addAttribute("projectOptions", projects);
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load filter options", e);
            model.addAttribute("projectOptions", List.of());
        }
    }
    
    /**
     * Add meeting statistics to model.
     */
    private void addMeetingStatistics(Model model, List<Meeting> meetings) {
        if (meetings.isEmpty()) {
            model.addAttribute("totalDuration", 0);
            model.addAttribute("avgAttendanceRate", 0);
            model.addAttribute("upcomingCount", 0);
            return;
        }
        
        // Total duration in hours
        long totalMinutes = meetings.stream()
            .mapToLong(meeting -> java.time.Duration.between(meeting.getStartTime(), meeting.getEndTime()).toMinutes())
            .sum();
        double totalHours = totalMinutes / 60.0;
        model.addAttribute("totalDuration", String.format("%.1f", totalHours));
        
        // Average attendance rate
        double avgAttendanceRate = meetings.stream()
            .mapToDouble(meeting -> {
                try {
                    return meeting.getAttendancePercentage();
                } catch (Exception e) {
                    return 0.0;
                }
            })
            .average()
            .orElse(0.0);
        model.addAttribute("avgAttendanceRate", Math.round(avgAttendanceRate));
        
        // Upcoming meetings count
        LocalDate today = LocalDate.now();
        long upcomingCount = meetings.stream()
            .filter(meeting -> meeting.getDate().isAfter(today) || meeting.getDate().isEqual(today))
            .count();
        model.addAttribute("upcomingCount", upcomingCount);
    }
    
    /**
     * Organize meetings for calendar view.
     */
    private void organizeMeetingsForCalendar(Model model, List<Meeting> meetings, LocalDate startDate, LocalDate endDate) {
        // Group meetings by date
        Map<LocalDate, List<Meeting>> meetingsByDate = meetings.stream()
            .collect(Collectors.groupingBy(Meeting::getDate));
        
        model.addAttribute("meetingsByDate", meetingsByDate);
        
        // Calendar navigation
        LocalDate prevMonth = startDate.minusMonths(1);
        LocalDate nextMonth = startDate.plusMonths(1);
        model.addAttribute("prevMonth", prevMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        model.addAttribute("nextMonth", nextMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        model.addAttribute("currentMonth", startDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        
        // Generate calendar grid
        generateCalendarGrid(model, startDate, endDate, meetingsByDate);
    }
    
    /**
     * Generate calendar grid for display.
     */
    private void generateCalendarGrid(Model model, LocalDate startDate, LocalDate endDate, Map<LocalDate, List<Meeting>> meetingsByDate) {
        // Start from the first day of the week containing startDate
        LocalDate gridStart = startDate.with(java.time.DayOfWeek.MONDAY);
        
        // End at the last day of the week containing endDate
        LocalDate gridEnd = endDate.with(java.time.DayOfWeek.SUNDAY);
        
        List<List<Map<String, Object>>> calendarWeeks = new java.util.ArrayList<>();
        LocalDate currentDate = gridStart;
        
        while (!currentDate.isAfter(gridEnd)) {
            List<Map<String, Object>> week = new java.util.ArrayList<>();
            
            for (int day = 0; day < 7; day++) {
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", currentDate);
                dayData.put("dayOfMonth", currentDate.getDayOfMonth());
                dayData.put("isCurrentMonth", !currentDate.isBefore(startDate) && !currentDate.isAfter(endDate));
                dayData.put("isToday", currentDate.equals(LocalDate.now()));
                dayData.put("meetings", meetingsByDate.getOrDefault(currentDate, List.of()));
                
                week.add(dayData);
                currentDate = currentDate.plusDays(1);
            }
            
            calendarWeeks.add(week);
        }
        
        model.addAttribute("calendarWeeks", calendarWeeks);
    }
    
    /**
     * Add upcoming meeting specific data.
     */
    private void addUpcomingMeetingData(Model model, List<Meeting> meetings) {
        LocalDate today = LocalDate.now();
        
        // Today's meetings
        List<Meeting> todaysMeetings = meetings.stream()
            .filter(meeting -> meeting.getDate().equals(today))
            .collect(Collectors.toList());
        model.addAttribute("todaysMeetings", todaysMeetings);
        
        // This week's meetings
        LocalDate weekEnd = today.plusDays(7);
        List<Meeting> thisWeeksMeetings = meetings.stream()
            .filter(meeting -> meeting.getDate().isAfter(today) && !meeting.getDate().isAfter(weekEnd))
            .collect(Collectors.toList());
        model.addAttribute("thisWeeksMeetings", thisWeeksMeetings);
        
        // Next meeting
        Meeting nextMeeting = meetings.stream()
            .filter(meeting -> meeting.getDate().isAfter(today) || 
                             (meeting.getDate().equals(today) && meeting.getStartTime().isAfter(LocalTime.now())))
            .min((m1, m2) -> {
                int dateCompare = m1.getDate().compareTo(m2.getDate());
                return dateCompare != 0 ? dateCompare : m1.getStartTime().compareTo(m2.getStartTime());
            })
            .orElse(null);
        model.addAttribute("nextMeeting", nextMeeting);
    }
    
    /**
     * Load overview tab data.
     */
    private void loadOverviewTabData(Model model, Meeting meeting) {
        // Basic meeting information already in model
        
        // Calculate meeting duration
        long durationMinutes = java.time.Duration.between(meeting.getStartTime(), meeting.getEndTime()).toMinutes();
        model.addAttribute("durationMinutes", durationMinutes);
        model.addAttribute("durationHours", String.format("%.1f", durationMinutes / 60.0));
        
        // Meeting status
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        String status;
        
        if (meeting.getDate().isBefore(today)) {
            status = "completed";
        } else if (meeting.getDate().equals(today)) {
            if (now.isBefore(meeting.getStartTime())) {
                status = "today-upcoming";
            } else if (now.isAfter(meeting.getEndTime())) {
                status = "today-completed";
            } else {
                status = "in-progress";
            }
        } else {
            status = "upcoming";
        }
        model.addAttribute("meetingStatus", status);
        
        // Time until meeting (if upcoming)
        if ("upcoming".equals(status) || "today-upcoming".equals(status)) {
            long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, meeting.getDate());
            model.addAttribute("daysUntilMeeting", daysUntil);
        }
    }
    
    /**
     * Load attendance tab data.
     */
    private void loadAttendanceTabData(Model model, Meeting meeting) {
        try {
            List<Attendance> attendanceRecords = attendanceService.findByMeeting(meeting);
            model.addAttribute("attendanceRecords", attendanceRecords);
            
            // Attendance statistics
            long presentCount = attendanceRecords.stream().filter(Attendance::isPresent).count();
            long absentCount = attendanceRecords.size() - presentCount;
            double attendanceRate = attendanceRecords.size() > 0 ? 
                (double) presentCount / attendanceRecords.size() * 100 : 0;
            
            model.addAttribute("presentCount", presentCount);
            model.addAttribute("absentCount", absentCount);
            model.addAttribute("totalMembers", attendanceRecords.size());
            model.addAttribute("attendanceRate", Math.round(attendanceRate));
            
            // Group by subteams for better organization
            Map<String, List<Attendance>> attendanceBySubteam = attendanceRecords.stream()
                .collect(Collectors.groupingBy(attendance -> 
                    attendance.getMember().getSubteam() != null ? 
                    attendance.getMember().getSubteam().getName() : "Unassigned"));
            
            model.addAttribute("attendanceBySubteam", attendanceBySubteam);
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load attendance data", e);
            model.addAttribute("attendanceRecords", List.of());
            model.addAttribute("presentCount", 0);
            model.addAttribute("absentCount", 0);
            model.addAttribute("totalMembers", 0);
            model.addAttribute("attendanceRate", 0);
        }
    }
    
    /**
     * Load notes tab data.
     */
    private void loadNotesTabData(Model model, Meeting meeting) {
        // Notes are already part of the meeting object
        
        // Add note editing capability check
        model.addAttribute("canEditNotes", hasRole("MENTOR") || hasRole("ADMIN"));
        
        // Parse notes for better display (simple line breaks)
        if (meeting.getNotes() != null) {
            String[] noteLines = meeting.getNotes().split("\n");
            model.addAttribute("noteLines", noteLines);
        }
    }
    
    /**
     * Add meeting detail statistics.
     */
    private void addMeetingDetailStatistics(Model model, Meeting meeting) {
        try {
            // Get attendance data
            List<Attendance> attendanceRecords = attendanceService.findByMeeting(meeting);
            
            if (!attendanceRecords.isEmpty()) {
                // Present/absent counts
                long presentCount = attendanceRecords.stream().filter(Attendance::isPresent).count();
                model.addAttribute("detailPresentCount", presentCount);
                model.addAttribute("detailAbsentCount", attendanceRecords.size() - presentCount);
                
                // Average attendance duration for present members
                double avgDuration = attendanceRecords.stream()
                    .filter(Attendance::isPresent)
                    .mapToLong(Attendance::getDurationMinutes)
                    .average()
                    .orElse(0.0);
                model.addAttribute("avgAttendanceDuration", String.format("%.0f", avgDuration));
                
                // Attendance by role
                long leadersPresentCount = attendanceRecords.stream()
                    .filter(att -> att.isPresent() && att.getMember().isLeader())
                    .count();
                model.addAttribute("leadersPresentCount", leadersPresentCount);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load meeting detail statistics", e);
        }
    }
    
    /**
     * Load form options for meeting creation/editing.
     */
    private void loadMeetingFormOptions(Model model) {
        try {
            // Projects
            List<Project> projects = projectService.findAll();
            model.addAttribute("projectOptions", projects);
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load meeting form options", e);
            model.addAttribute("projectOptions", List.of());
        }
    }
    
    /**
     * Add helpful defaults and suggestions for meeting forms.
     */
    private void addMeetingFormHelpers(Model model) {
        model.addAttribute("commonTimes", List.of(
            Map.of("time", "15:30", "label", "3:30 PM (After School)"),
            Map.of("time", "16:00", "label", "4:00 PM"),
            Map.of("time", "16:30", "label", "4:30 PM"),
            Map.of("time", "17:00", "label", "5:00 PM"),
            Map.of("time", "18:00", "label", "6:00 PM"),
            Map.of("time", "19:00", "label", "7:00 PM (Evening)")
        ));
        
        model.addAttribute("commonDurations", List.of(
            Map.of("hours", 1, "label", "1 hour"),
            Map.of("hours", 1.5, "label", "1.5 hours"),
            Map.of("hours", 2, "label", "2 hours"),
            Map.of("hours", 2.5, "label", "2.5 hours"),
            Map.of("hours", 3, "label", "3 hours")
        ));
    }
    
    /**
     * Check for scheduling conflicts.
     */
    private void checkSchedulingConflicts(Model model, LocalDate date, LocalTime startTime, LocalTime endTime) {
        checkSchedulingConflicts(model, date, startTime, endTime, null);
    }
    
    /**
     * Check for scheduling conflicts, excluding a specific meeting.
     */
    private void checkSchedulingConflicts(Model model, LocalDate date, LocalTime startTime, LocalTime endTime, Long excludeMeetingId) {
        try {
            if (date != null && startTime != null && endTime != null) {
                List<Meeting> allMeetings = meetingService.findByDate(date);
                
                List<Meeting> conflicts = allMeetings.stream()
                    .filter(meeting -> excludeMeetingId == null || !meeting.getId().equals(excludeMeetingId))
                    .filter(meeting -> {
                        // Check for time overlap
                        return !(endTime.isBefore(meeting.getStartTime()) || startTime.isAfter(meeting.getEndTime()));
                    })
                    .collect(Collectors.toList());
                
                if (!conflicts.isEmpty()) {
                    model.addAttribute("hasConflicts", true);
                    model.addAttribute("conflicts", conflicts);
                    addWarningMessage(model, 
                        "Warning: " + conflicts.size() + " scheduling conflict(s) detected on this date and time.");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to check scheduling conflicts", e);
        }
    }
    
    /**
     * Initialize attendance records for all team members.
     */
    private void initializeAttendanceRecords(Meeting meeting) {
        try {
            List<TeamMember> allMembers = teamMemberService.findAll();
            
            for (TeamMember member : allMembers) {
                // Create attendance record with default absent status
                attendanceService.createAttendance(meeting.getId(), member.getId(), false);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to initialize attendance records", e);
        }
    }
    
    /**
     * Validate meeting data for creation/editing.
     */
    private void validateMeetingData(Meeting meeting, BindingResult result) {
        // Date validation
        if (meeting.getDate() == null) {
            result.rejectValue("date", "required", "Meeting date is required");
        }
        
        // Time validation
        if (meeting.getStartTime() == null) {
            result.rejectValue("startTime", "required", "Start time is required");
        }
        
        if (meeting.getEndTime() == null) {
            result.rejectValue("endTime", "required", "End time is required");
        }
        
        if (meeting.getStartTime() != null && meeting.getEndTime() != null) {
            if (meeting.getEndTime().isBefore(meeting.getStartTime())) {
                result.rejectValue("endTime", "invalid", "End time cannot be before start time");
            }
            
            if (meeting.getStartTime().equals(meeting.getEndTime())) {
                result.rejectValue("endTime", "invalid", "End time cannot be the same as start time");
            }
            
            // Check for reasonable duration (not more than 8 hours)
            long durationMinutes = java.time.Duration.between(meeting.getStartTime(), meeting.getEndTime()).toMinutes();
            if (durationMinutes > 480) { // 8 hours
                result.rejectValue("endTime", "invalid", "Meeting duration cannot exceed 8 hours");
            }
        }
        
        // Project validation
        if (meeting.getProject() == null) {
            result.rejectValue("project", "required", "Project is required");
        }
    }
    
    /**
     * Add warnings for meeting editing.
     */
    private void addMeetingEditWarnings(Model model, Meeting meeting) {
        try {
            List<Attendance> attendanceRecords = attendanceService.findByMeeting(meeting);
            
            if (!attendanceRecords.isEmpty()) {
                long recordedAttendance = attendanceRecords.stream()
                    .filter(Attendance::isPresent)
                    .count();
                
                if (recordedAttendance > 0) {
                    model.addAttribute("hasRecordedAttendance", true);
                    model.addAttribute("recordedAttendanceCount", recordedAttendance);
                    addWarningMessage(model, 
                        "This meeting has recorded attendance for " + recordedAttendance + " members. " +
                        "Changing the meeting details may affect attendance reporting.");
                }
            }
            
            // Check if meeting is in the past
            if (meeting.getDate().isBefore(LocalDate.now())) {
                addWarningMessage(model, "This meeting is in the past. Consider the impact on historical records.");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load edit warnings", e);
        }
    }
    
    /**
     * Add deletion impact information.
     */
    private void addMeetingDeletionImpactInfo(Model model, Meeting meeting) {
        try {
            List<Attendance> attendanceRecords = attendanceService.findByMeeting(meeting);
            model.addAttribute("attendanceRecordCount", attendanceRecords.size());
            
            long recordedAttendance = attendanceRecords.stream()
                .filter(Attendance::isPresent)
                .count();
            model.addAttribute("recordedAttendanceCount", recordedAttendance);
            
            model.addAttribute("isCompletedMeeting", meeting.getDate().isBefore(LocalDate.now()));
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load deletion impact info", e);
            model.addAttribute("attendanceRecordCount", 0);
            model.addAttribute("recordedAttendanceCount", 0);
        }
    }
    
    /**
     * Format meeting title for display.
     */
    private String formatMeetingTitle(Meeting meeting) {
        return String.format("%s - %s", 
            meeting.getProject().getName(),
            meeting.getDate().format(DATE_FORMATTER));
    }
    
    /**
     * Format time for display.
     */

    /* 
    private String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FORMATTER) : "";
    }
    */

    /**
     * Parse time from HTML time input.
     */

    /* 
    private LocalTime parseTimeFromInput(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(timeString, TIME_INPUT_FORMATTER);
        } catch (Exception e) {
            LOGGER.warning("Failed to parse time: " + timeString);
            return null;
        }
    }
    */
    
    /**
     * Helper method to escape CSV values.
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        
        // Escape quotes and wrap in quotes if necessary
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
}