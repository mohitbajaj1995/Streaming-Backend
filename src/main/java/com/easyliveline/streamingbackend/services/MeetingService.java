package com.easyliveline.streamingbackend.services;

import com.easyliveline.streamingbackend.dto.MeetingWithOwnerName;
import com.easyliveline.streamingbackend.interfaces.*;
import com.easyliveline.streamingbackend.util.ExceptionWrapper;
import com.easyliveline.streamingbackend.models.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final ZoomRepository zoomRepository;
    private final CommonQueryService commonQueryService;
    private final UserRepository userRepository;

    @Autowired
    public MeetingService(MeetingRepository meetingRepository, ZoomRepository zoomRepository,
                          CommonQueryService commonQueryService, UserRepository userRepository) {
        this.meetingRepository = meetingRepository;
        this.zoomRepository = zoomRepository;
        this.commonQueryService = commonQueryService;
        this.userRepository = userRepository;
    }

    // Method to create a meeting
    public Meeting createMeeting(MeetingCreateRequest requestBody) {
        log.info("Creating new meeting with name: {}", requestBody.getName());
        return ExceptionWrapper.handle(() -> {
            // Logic to create a meeting
            Meeting meeting = new Meeting();
            meeting.setName(requestBody.getName());
            meeting.setEmail(requestBody.getEmail());
            meeting.setPassword(requestBody.getPassword());
            meeting.setMeetingNumber(requestBody.getMeetingNumber());
            meeting.setMeetingPassword(requestBody.getMeetingPassword());
            meeting.setActivated(true);

            Zoom zoom = zoomRepository.getReferenceById(requestBody.getZoomId());
            log.debug("Using Zoom configuration with ID: {}", requestBody.getZoomId());
            meeting.setZoom(zoom);

            // Save the meeting to the database
            Meeting savedMeeting = meetingRepository.save(meeting);
            log.info("Successfully created meeting with ID: {}", savedMeeting.getId());
            return savedMeeting;
        });
    }

    @Transactional
    public Meeting findMeetingWithZoomById(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found with id " + meetingId));

        Hibernate.initialize(meeting.getZoom());
        return meeting;
    }


    @Transactional
    public void deleteMeeting(Long meetingId) {
        log.info("Deleting meeting with ID: {}", meetingId);
        ExceptionWrapper.handleVoid(() -> {
            try {
                meetingRepository.deleteByIdCustom(meetingId);
                log.info("Successfully deleted meeting with ID: {}", meetingId);
            } catch (Exception e) {
                log.error("Failed to delete meeting with ID: {}", meetingId, e);
                throw e;
            }
        });
    }

    public Meeting updateMeeting(Long id, MeetingUpdateRequest requestBody) {
        log.info("Updating meeting with ID: {}", id);
        return ExceptionWrapper.handle(() -> {
            Meeting meeting = meetingRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Meeting not found with ID: {}", id);
                        return new RuntimeException("Meeting not found");
                    });
            log.debug("Found meeting with ID: {}, current name: {}", id, meeting.getName());

            meeting.setName(requestBody.getName());
            meeting.setEmail(requestBody.getEmail());
            meeting.setActivated(requestBody.isActivated());
            meeting.setPassword(requestBody.getPassword());
            meeting.setMeetingNumber(requestBody.getMeetingNumber());
            meeting.setMeetingPassword(requestBody.getMeetingPassword());
            log.debug("Updated meeting details - name: {}, email: {}, activated: {}", 
                    requestBody.getName(), requestBody.getEmail(), requestBody.isActivated());

            if (requestBody.getZoomId() != null && !requestBody.getZoomId().equals(meeting.getZoom().getId())) {
                log.debug("Changing Zoom configuration from ID: {} to ID: {}", 
                        meeting.getZoom().getId(), requestBody.getZoomId());
                Zoom zoom = zoomRepository.getReferenceById(requestBody.getZoomId());
                meeting.setZoom(zoom);
            }

            Meeting updatedMeeting = meetingRepository.save(meeting);
            log.info("Successfully updated meeting with ID: {}", id);
            return updatedMeeting;
        });
    }
//
//    public List<UnassignedMeetingOptionsDTO> getUnassignedMeetingByOwnerId() {
//        log.info("Fetching unassigned meetings for current owner");
//        return ExceptionWrapper.handle(() -> {
//            Long ownerId = commonQueryService.resolveParent();
//            log.debug("Resolved parent ID: {}", ownerId);
//
//            List<UnassignedMeetingOptionsDTO> meetings = meetingRepository.getMeetingsWithoutSlotForOwner(ownerId);
//            log.debug("Found {} unassigned meetings for owner ID: {}", meetings.size(), ownerId);
//            return meetings;
//        });
//    }
//
//    @Transactional
//    public void assignSlotToMeeting(Long meetingId, Long slotId) {
//        log.info("Assigning slot ID: {} to meeting ID: {}", slotId, meetingId);
//        ExceptionWrapper.handleVoid(() -> {
//            try {
//                slotRepository.assignMeetingToSlot(slotId, meetingId);
//                log.info("Successfully assigned slot ID: {} to meeting ID: {}", slotId, meetingId);
//            } catch (Exception e) {
//                log.error("Failed to assign slot ID: {} to meeting ID: {}", slotId, meetingId, e);
//                throw e;
//            }
//        });
//    }
//
//    @Transactional
//    public void removeSlotFromMeeting(Long meetingId) {
//        log.info("Removing slot from meeting ID: {}", meetingId);
//        ExceptionWrapper.handleVoid(() -> {
//            try {
//                slotRepository.removeSlotFromMeeting(meetingId);
//                log.info("Successfully removed slot from meeting ID: {}", meetingId);
//            } catch (Exception e) {
//                log.error("Failed to remove slot from meeting ID: {}", meetingId, e);
//                throw e;
//            }
//        });
//    }

    public Page<MeetingWithOwnerName> getFilteredMeetings(FilterRequest sortFilterBody) {
        log.info("Fetching filtered meetings with filters: {}", sortFilterBody);
        return ExceptionWrapper.handle(() -> {
//            Long parentId = commonQueryService.resolveParent();
//            log.debug("Resolved parent ID: {}", parentId);

            Map<String, String> columnAliasMap = Map.of(
                    "hostPlanExpiry", "h.endAt"
    //                "locked", "e.locked",
    //                "host_username", "ho.username"
            );
            log.debug("Column alias map created");

            Map<String, Object> dynamicParams = new HashMap<>();
            String whereClause = getStringBuilder(sortFilterBody, dynamicParams, null);
            log.debug("Where clause generated: {}", whereClause);

            Page<MeetingWithOwnerName> result = commonQueryService.fetchWithCustomFilters(
                    MeetingWithOwnerName.class,
                    Meeting.class,// Your entity
                    sortFilterBody,
                    columnAliasMap,// Pagination, sorting, filters
                    Optional.of("SELECT new com.easyliveline.streamingbackend.dto.MeetingWithOwnerName(e.id, e.name, e.email, e.password, e.meetingNumber, e.meetingPassword, e.createdAt, e.zoom.id)"),
                    Optional.of("FROM Meeting e"),
                    Optional.of(""),
                    Optional.of(whereClause),
                    Optional.empty(),
                    dynamicParams
            );

            log.info("Fetched {} meetings (page {} of {}, size {})", 
                    result.getNumberOfElements(), 
                    result.getNumber() + 1, 
                    result.getTotalPages(),
                    result.getSize());

            return result;
        });
    }

    private static String getStringBuilder(FilterRequest sortFilterBody, Map<String, Object> dynamicParams, Long userId) {
        return ExceptionWrapper.handle(() -> {
//            log.debug("Building where clause for parent ID: {}", userId);
            StringBuilder whereClause = new StringBuilder();
//            dynamicParams.put("parentId", userId);

            if(sortFilterBody.getGlobalFilter() != null && !sortFilterBody.getGlobalFilter().isEmpty()){
                log.debug("Adding global filter: {}", sortFilterBody.getGlobalFilter());
                whereClause.append(" AND (e.name ILIKE :globalFilter OR a.name ILIKE :globalFilter OR hp.username ILIKE :globalFilter OR h.username ILIKE :globalFilter)");
                dynamicParams.put("globalFilter", "%" + sortFilterBody.getGlobalFilter() + "%");
            }

            if (!sortFilterBody.getColumnFilters().isEmpty()) {
                log.debug("Processing {} column filters", sortFilterBody.getColumnFilters().size());
            }

            for (FilterRequest.ColumnFilter filter : sortFilterBody.getColumnFilters()) {
                log.debug("Processing column filter - ID: {}, Value: {}", filter.getId(), filter.getValue());
                switch (filter.getId()) {
                    case "name" -> {
                        whereClause.append(" AND e.name ILIKE :name");
                        dynamicParams.put("name", "%" + filter.getValue() + "%");
                    }
                    case "meetingNumber" -> {
                        whereClause.append(" AND e.meetingNumber LIKE :meetingNumber");
                        dynamicParams.put("meetingNumber", "%" + filter.getValue() + "%");
                    }
                }
            }

            String result = whereClause.toString();
            log.debug("Final where clause: {}", result);
            return result;
        });
    }
}
