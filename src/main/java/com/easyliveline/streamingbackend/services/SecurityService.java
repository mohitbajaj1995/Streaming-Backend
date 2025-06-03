package com.easyliveline.streamingbackend.services;

import com.easyliveline.streamingbackend.dto.*;
import com.easyliveline.streamingbackend.enums.RoleType;
import com.easyliveline.streamingbackend.interfaces.*;
import com.easyliveline.streamingbackend.util.ExceptionWrapper;
import com.easyliveline.streamingbackend.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SecurityService {

    private final UserRepository userRepository;
    private final SubscriberRepository subscriberRepository;
    private final MasterRepository masterRepository;
    private final SuperMasterRepository superMasterRepository;
    private final ManagerRepository managerRepository;
    private final MeetingRepository meetingRepository;
    private final CommonQueryService commonQueryService;
    private final ZoomRepository zoomRepository;

    @Autowired
    public SecurityService(UserRepository userRepository, SubscriberRepository subscriberRepository, MasterRepository masterRepository,SuperMasterRepository superMasterRepository, ManagerRepository managerRepository,
                           MeetingRepository meetingRepository, CommonQueryService commonQueryService, ZoomRepository zoomRepository) {
        this.userRepository = userRepository;
        this.subscriberRepository = subscriberRepository;
        this.masterRepository = masterRepository;
        this.superMasterRepository = superMasterRepository;
        this.managerRepository = managerRepository;
        this.meetingRepository = meetingRepository;
        this.commonQueryService = commonQueryService;
        this.zoomRepository = zoomRepository;
    }

    public boolean hasPermissionForTask(String task, Long childUserId) {
        log.debug("Checking permission for task: {} and child user ID: {}", task, childUserId);
        return ExceptionWrapper.handle(() -> {
            boolean result = switch (task) {
                case "UPDATE_USER", "RECHARGE_HOST", "RECHARGE_POINTS", "DELETE_MANAGER", "REVERSE_POINTS_TRANSACTION", "ADJUST_HOST" ->
                        isEligibleForTheTask(childUserId);
                default -> {
                    log.debug("Unknown task: {}, denying permission", task);
                    yield false;
                }
            };
            log.debug("Permission check result for task: {} and child user ID: {}: {}", task, childUserId, result);
            return result;
        });
    }

//    public void isZoomAccountDeletableAndHasPermissionToDelete(Long zoomId) {
//        log.debug("Checking if Zoom account with ID: {} is deletable and user has permission", zoomId);
//        ExceptionWrapper.handleVoid(() -> {
//            Long userId = commonQueryService.resolveParent();
//            log.debug("Resolved parent user ID: {}", userId);
//
//            ZoomStatsDTO zoomStats = zoomRepository.fetchZoomStats(zoomId);
//            if (zoomStats == null) {
//                log.error("Zoom account not found with ID: {}", zoomId);
//                throw new ResourceNotFoundException("Zoom account not found with ID: " + zoomId);
//            }
//            log.debug("Found Zoom account stats - parent ID: {}, meeting count: {}", zoomStats.parentId(), zoomStats.meetingCount());
//
//            if (zoomStats.meetingCount() > 0) {
//                log.error("Cannot delete Zoom account with ID: {} because it has {} active meetings", zoomId, zoomStats.meetingCount());
//                throw new AccessDeniedException("Zoom account has active meetings and cannot be deleted");
//            }
//
//            if (!zoomStats.parentId().equals(userId)) {
//                log.error("User ID: {} does not have permission to delete Zoom account with ID: {} owned by user ID: {}",
//                        userId, zoomId, zoomStats.parentId());
//                throw new AccessDeniedException("You do not have permission to delete this Zoom account");
//            }
//
//            log.info("Zoom account with ID: {} is deletable and user ID: {} has permission", zoomId, userId);
//        });
//    }
//
//    public void isSubHostDeletableAndHasPermissionToDelete(Long subHostId) {
//        log.debug("Checking if SubHost with ID: {} is deletable and user has permission", subHostId);
//        Long userId = commonQueryService.resolveParent();
//        log.debug("Resolved parent user ID: {}", userId);
//
//        SubHostHasSlotRecord subHostMeta = subHostRepository.findSubHostSlotPresenceById(subHostId);
//        if (subHostMeta == null) {
//            log.error("SubHost not found with ID: {}", subHostId);
//            throw new ResourceNotFoundException("SubHost not found with ID: " + subHostId);
//        }
//        log.debug("Found SubHost metadata - host ID: {}, has slot: {}", subHostMeta.hostId(), subHostMeta.hasSlot());
//
//        if (subHostMeta.hasSlot()) {
//            log.error("Cannot delete SubHost with ID: {} because it has an assigned slot", subHostId);
//            throw new AccessDeniedException("SubHost has assigned slot and cannot be deleted");
//        }
//
//        if (!subHostMeta.hostId().equals(userId)) {
//            log.error("User ID: {} does not have permission to delete SubHost with ID: {} owned by host ID: {}",
//                    userId, subHostId, subHostMeta.hostId());
//            throw new AccessDeniedException("You do not have permission to delete this SubHost");
//        }
//
//        log.info("SubHost with ID: {} is deletable and user ID: {} has permission", subHostId, userId);
//    }
//
//    public void isMeetingDeletableAndHasPermissionToDelete(Long meetingId) {
//        log.debug("Checking if Meeting with ID: {} is deletable and user has permission", meetingId);
//        Long userId = commonQueryService.resolveParent();
//        log.debug("Resolved parent user ID: {}", userId);
//
//        MeetingOwnerSlotForSecurityDTO meetingMeta = meetingRepository.findOwnerIdAndSlotIdByMeetingId(meetingId);
//        if (meetingMeta == null) {
//            log.error("Meeting not found with ID: {}", meetingId);
//            throw new ResourceNotFoundException("Meeting not found with ID: " + meetingId);
//        }
//        log.debug("Found Meeting metadata - owner ID: {}, slot ID: {}", meetingMeta.ownerId(), meetingMeta.slotId());
//
//        if (!meetingMeta.ownerId().equals(userId)) {
//            log.error("User ID: {} does not have permission to delete Meeting with ID: {} owned by user ID: {}",
//                    userId, meetingId, meetingMeta.ownerId());
//            throw new AccessDeniedException("You do not have permission to delete this Meeting");
//        }
//
//        if (meetingMeta.slotId() != null) {
//            log.error("Cannot delete Meeting with ID: {} because it is assigned to slot ID: {}", meetingId, meetingMeta.slotId());
//            throw new AccessDeniedException("Meeting has assigned to a slot and cannot be deleted");
//        }
//
//        log.info("Meeting with ID: {} is deletable and user ID: {} has permission", meetingId, userId);
//    }
//
//    public void isSlotDeletableAndHasPermissionToDeleteSlot(Long slotId) {
//        log.debug("Checking if Slot with ID: {} is deletable and user has permission", slotId);
//        Long userId = commonQueryService.resolveParent();
//        log.debug("Resolved parent user ID: {}", userId);
//
//        SlotDeleteSecurityCheck slotMeta = slotRepository.findSlotSummaryById(slotId);
//        if (slotMeta == null) {
//            log.error("Slot not found with ID: {}", slotId);
//            throw new ResourceNotFoundException("Slot not found with ID: " + slotId);
//        }
//        log.debug("Found Slot metadata - created by: {}, has meeting: {}, has subHost: {}, participants count: {}",
//                slotMeta.createdBy(), slotMeta.hasMeetingId(), slotMeta.hasSubHost(), slotMeta.participantsCount());
//
//        if (!slotMeta.createdBy().equals(userId)) {
//            log.error("User ID: {} does not have permission to delete Slot with ID: {} created by user ID: {}",
//                    userId, slotId, slotMeta.createdBy());
//            throw new AccessDeniedException("You do not have permission to delete this Slot");
//        }
//
//        if (slotMeta.hasMeetingId()) {
//            log.error("Cannot delete Slot with ID: {} because it has an assigned meeting", slotId);
//            throw new AccessDeniedException("Slot has assigned meeting and cannot be deleted");
//        }
//
//        if (slotMeta.hasSubHost()) {
//            log.error("Cannot delete Slot with ID: {} because it has an assigned subHost", slotId);
//            throw new AccessDeniedException("Slot has assigned subHost and cannot be deleted");
//        }
//
//        if (slotMeta.participantsCount() > 0) {
//            log.error("Cannot delete Slot with ID: {} because it has {} assigned participants", slotId, slotMeta.participantsCount());
//            throw new AccessDeniedException("Slot has assigned participants and cannot be deleted");
//        }
//
//        log.info("Slot with ID: {} is deletable and user ID: {} has permission", slotId, userId);
//    }
//
//    public void isHostDeletableAndHasPermissionToDelete(Long hostId){
//        log.debug("Checking if Host with ID: {} is deletable and user has permission", hostId);
//        Long userId = commonQueryService.resolveParent();
//        log.debug("Resolved parent user ID: {}", userId);
//
//        HostDeleteServiceCheck hostMeta = hostRepository.findHostSummaryById(hostId);
//        if (hostMeta == null) {
//            log.error("Host not found with ID: {}", hostId);
//            throw new ResourceNotFoundException("Host not found with ID: " + hostId);
//        }
//        log.debug("Found Host metadata - parent ID: {}, slots count: {}, subHosts count: {}, participants count: {}",
//                hostMeta.parentId(), hostMeta.slotsCount(), hostMeta.subHostsCount(), hostMeta.participantsCount());
//
//        if (!hostMeta.parentId().equals(userId)) {
//            log.error("User ID: {} does not have permission to delete Host with ID: {} owned by parent ID: {}",
//                    userId, hostId, hostMeta.parentId());
//            throw new AccessDeniedException("You do not have permission to delete this Host");
//        }
//
//        if (hostMeta.slotsCount() > 0) {
//            log.error("Cannot delete Host with ID: {} because it has {} assigned slots", hostId, hostMeta.slotsCount());
//            throw new AccessDeniedException("Host has assigned slots and cannot be deleted");
//        }
//
//        if (hostMeta.subHostsCount() > 0) {
//            log.error("Cannot delete Host with ID: {} because it has {} assigned subHosts", hostId, hostMeta.subHostsCount());
//            throw new AccessDeniedException("Host has assigned subHosts and cannot be deleted");
//        }
//
//        if (hostMeta.participantsCount() > 0) {
//            log.error("Cannot delete Host with ID: {} because it has {} assigned participants", hostId, hostMeta.participantsCount());
//            throw new AccessDeniedException("Host has assigned participants and cannot be deleted");
//        }
//
//        log.info("Host with ID: {} is deletable and user ID: {} has permission", hostId, userId);
//    }
//
//    public void isMasterDeletableAndHasPermissionToDelete(Long masterId){
//        log.debug("Checking if Master with ID: {} is deletable and user has permission", masterId);
//        Long userId = commonQueryService.resolveParent();
//        log.debug("Resolved parent user ID: {}", userId);
//
//        MasterDeleteSecurityCheck masterMeta = masterRepository.findMasterSummaryById(masterId);
//        if (masterMeta == null) {
//            log.error("Master not found with ID: {}", masterId);
//            throw new ResourceNotFoundException("Master not found with ID: " + masterId);
//        }
//        log.debug("Found Master metadata - parent ID: {}, slots count: {}, hosts count: {}, meetings count: {}, zooms count: {}",
//                masterMeta.parentId(), masterMeta.slotsCount(), masterMeta.hostsCount(), masterMeta.meetingsCount(), masterMeta.zoomsCount());
//
//        if (!masterMeta.parentId().equals(userId)) {
//            log.error("User ID: {} does not have permission to delete Master with ID: {} owned by parent ID: {}",
//                    userId, masterId, masterMeta.parentId());
//            throw new AccessDeniedException("You do not have permission to delete this Master");
//        }
//
//        if (masterMeta.slotsCount() > 0) {
//            log.error("Cannot delete Master with ID: {} because it has {} slots", masterId, masterMeta.slotsCount());
//            throw new AccessDeniedException("Master has slots and cannot be deleted");
//        }
//
//        if (masterMeta.hostsCount() > 0) {
//            log.error("Cannot delete Master with ID: {} because it has {} hosts", masterId, masterMeta.hostsCount());
//            throw new AccessDeniedException("Master has hosts and cannot be deleted");
//        }
//
//        if (masterMeta.meetingsCount() > 0) {
//            log.error("Cannot delete Master with ID: {} because it has {} meetings", masterId, masterMeta.meetingsCount());
//            throw new AccessDeniedException("Master has meetings and cannot be deleted");
//        }
//
//        if(masterMeta.zoomsCount() > 0) {
//            log.error("Cannot delete Master with ID: {} because it has {} Zoom accounts", masterId, masterMeta.zoomsCount());
//            throw new AccessDeniedException("Master has Zoom accounts and cannot be deleted");
//        }
//
//        log.info("Master with ID: {} is deletable and user ID: {} has permission", masterId, userId);
//    }
//
//    public void hasPermissionToRemoveSlotFromSubHost(Long subHostId) {
//        log.debug("Checking if user has permission to remove slot from SubHost with ID: {}", subHostId);
//        Long userId = commonQueryService.resolveParent();
//        log.debug("Resolved parent user ID: {}", userId);
//
//        SubHostHasSlotRecord subHostMeta = subHostRepository.findSubHostSlotPresenceById(subHostId);
//        if (subHostMeta == null) {
//            log.error("SubHost not found with ID: {}", subHostId);
//            throw new ResourceNotFoundException("SubHost not found with ID: " + subHostId);
//        }
//        log.debug("Found SubHost metadata - host ID: {}, has slot: {}", subHostMeta.hostId(), subHostMeta.hasSlot());
//
//        if (!subHostMeta.hasSlot()) {
//            log.error("SubHost with ID: {} does not have an assigned slot", subHostId);
//            throw new AccessDeniedException("SubHost does not have assigned slot");
//        }
//
//        if (!subHostMeta.hostId().equals(userId)) {
//            log.error("User ID: {} does not have permission to remove slot from SubHost with ID: {} owned by host ID: {}",
//                    userId, subHostId, subHostMeta.hostId());
//            throw new AccessDeniedException("You do not have permission to remove slot from this SubHost");
//        }
//
//        log.info("User ID: {} has permission to remove slot from SubHost with ID: {}", userId, subHostId);
//    }
//
//    public void hasPermissionToAssignSubHost(Long slotId, Long subHostId) {
//        log.debug("Checking if user has permission to assign SubHost ID: {} to Slot ID: {}", subHostId, slotId);
//        ExceptionWrapper.handleVoid(() -> {
//            Long currentUserId = commonQueryService.resolveParent();
//            log.debug("Resolved current user ID: {}", currentUserId);
//
//            SlotRecordSecurityCheck slotRecord = slotRepository.findSlotRecordById(slotId)
//                    .orElseThrow(() -> {
//                        log.error("Slot not found with ID: {}", slotId);
//                        return new ResourceNotFoundException("Slot not found with ID: " + slotId);
//                    });
//            log.debug("Found slot record - host ID: {}, subHost ID: {}", slotRecord.hostId(), slotRecord.subHostId());
//
//            // Validate if the current user is the host of the slot
//            if (!slotRecord.hostId().equals(currentUserId)) {
//                log.error("User ID: {} does not have permission to assign SubHost to Slot ID: {} owned by host ID: {}",
//                        currentUserId, slotId, slotRecord.hostId());
//                throw new AccessDeniedException("Slot does not belong to the current user");
//            }
//
//            // Validate if the subHost is already assigned to the slot
//            if (slotRecord.subHostId() != null) {
//                if (slotRecord.subHostId().equals(subHostId)) {
//                    log.error("SubHost ID: {} is already assigned to Slot ID: {}", subHostId, slotId);
//                    throw new AccessDeniedException("Same SubHost already assigned to this slot");
//                }
//                log.error("Slot ID: {} already has SubHost ID: {} assigned", slotId, slotRecord.subHostId());
//                throw new AccessDeniedException("SubHost already assigned to another slot");
//            }
//
//            SubHostIdsForSecurityDTO subHost = subHostRepository.findSubHostIdsById(subHostId)
//                    .orElseThrow(() -> {
//                        log.error("SubHost not found with ID: {}", subHostId);
//                        return new ResourceNotFoundException("SubHost not found with ID: " + subHostId);
//                    });
//            log.debug("Found SubHost - host ID: {}, enabled: {}, slot ID: {}",
//                    subHost.hostId(), subHost.enabled(), subHost.slotId());
//
//            if (!subHost.hostId().equals(currentUserId)) {
//                log.error("User ID: {} does not have permission to assign SubHost ID: {} owned by host ID: {}",
//                        currentUserId, subHostId, subHost.hostId());
//                throw new AccessDeniedException("SubHost does not belong to the current user");
//            }
//
//            if (!subHost.enabled()) {
//                log.error("Cannot assign disabled SubHost ID: {} to Slot ID: {}", subHostId, slotId);
//                throw new AccessDeniedException("SubHost is not enabled");
//            }
//
//            if (subHost.slotId() != null) {
//                if (subHost.slotId().equals(slotId)) {
//                    log.error("SubHost ID: {} is already assigned to Slot ID: {}", subHostId, slotId);
//                    throw new AccessDeniedException("Same SubHost already assigned to this slot");
//                }
//                log.error("SubHost ID: {} is already assigned to another Slot ID: {}", subHostId, subHost.slotId());
//                throw new AccessDeniedException("SubHost already assigned to another slot");
//            }
//        });
//    }
//
//    public void hasPermissionToRemoveSubHost(Long slotId) {
//        ExceptionWrapper.handleVoid(() -> {
//            Long currentUserId = JwtUtil.getUserIdFromJWT();
//
//            SlotHostSubHostSecurityCheckRecord slot = slotRepository.findHostAndSubHostBySlotId(slotId)
//                    .orElseThrow(() -> new ResourceNotFoundException("Slot not found with ID: " + slotId));
//
//            if (!slot.hostId().equals(currentUserId)) {
//                throw new AccessDeniedException("Slot does not belong to the current user");
//            }
//
//            if (slot.subHostId() == null) {
//                throw new AccessDeniedException("No SubHost assigned to this slot");
//            }
//        });
//    }
//
//    public void hasPermissionToAssignMeeting(Long slotId, Long meetingId) {
//        ExceptionWrapper.handleVoid(() -> {
//            Long currentUserId = JwtUtil.getUserIdFromJWT();
//
//            SlotMeetingSecurityCheckDTO slot = slotRepository.findSlotLockAndMeeting(slotId);
//
//            if (slot == null) {
//                throw new ResourceNotFoundException("Slot not found with ID: " + slotId);
//            }
//
//            if (slot.isLocked()) {
//                throw new AccessDeniedException("Slot is locked");
//            }
//
//            if (slot.meetingId() != null && slot.meetingId().equals(meetingId)) {
//                throw new AccessDeniedException("Meeting already assigned to this slot");
//            }
//
//            if (!slotRepository.isUserVisibleToSlot(slotId, currentUserId)) {
//                throw new AccessDeniedException("You don't have permission to access this slot");
//            }
//
//            MeetingOwnerSlotForSecurityDTO meetingDTO = meetingRepository.findOwnerIdAndSlotIdByMeetingId(meetingId);
//
//            if (meetingDTO == null) {
//                throw new ResourceNotFoundException("Meeting not found with ID: " + meetingId);
//            }
//
//            if (!meetingDTO.ownerId().equals(currentUserId)) {
//                throw new AccessDeniedException("You don't have permission to access this meeting");
//            }
//
//            if (meetingDTO.slotId() != null) {
//                if (meetingDTO.slotId().equals(slotId)) {
//                    throw new AccessDeniedException("Same Meeting already assigned to this slot");
//                }
//                throw new AccessDeniedException("Meeting already assigned to another slot");
//            }
//        });
//    }
//
//    public void hasPermissionToAssignSlotToParticipant(Long participantId, Long slotId) {
//        ExceptionWrapper.handleVoid(() -> {
//            Long currentUserId = JwtUtil.getUserIdFromJWT();
//
//            SlotRecordSecurityCheck slotRecord = slotRepository.findSlotRecordById(slotId)
//                    .orElseThrow(() -> new ResourceNotFoundException("Slot not found with ID: " + slotId));
//
//            if (!slotRecord.hostId().equals(currentUserId)) {
//                throw new AccessDeniedException("Slot does not belong to the Host");
//            }
//
//            ParticipantSlotHostIdsSecurityDTO participantInfo = participantRepository.findSlotAndHostIdsByParticipantId(participantId);
//
//            if (!participantInfo.hostId().equals(currentUserId)) {
//                throw new AccessDeniedException("Participant does not belong to the Host");
//            }
//
//            if (participantInfo.slotId() != null) {
//                if (participantInfo.slotId().equals(slotId)) {
//                    throw new AccessDeniedException("Participant already assigned to the slot");
//                }
//                throw new AccessDeniedException("Slot already assigned to this Participant");
//            }
//        });
//    }
//
//    public void hasPermissionToRemoveMeeting(Long slotId) {
//        ExceptionWrapper.handleVoid(() -> {
//            Long currentUserId = JwtUtil.getUserIdFromJWT();
//
//            SlotMeetingInfoSecurityCheckRecord slot = slotRepository.findSlotMeetingInfo(slotId);
//
//            if (slot == null) {
//                throw new ResourceNotFoundException("Slot not found with ID: " + slotId);
//            }
//
//            if (slot.meetingId() == null) {
//                throw new AccessDeniedException("No meeting assigned to this slot");
//            }
//
//            if (!slotRepository.isUserVisibleToSlot(slotId, currentUserId)) {
//                throw new AccessDeniedException("You don't have permission to access this slot");
//            }
//
//            if (!slot.meetingOwnerId().equals(currentUserId)) {
//                throw new AccessDeniedException("You don't have permission to access this meeting");
//            }
//        });
//    }
//
//    public void hasPermissionToAssignSlot(Long meetingId, Long slotId) {
//        ExceptionWrapper.handleVoid(() -> {
//            Long currentUserId = JwtUtil.getUserIdFromJWT();
//
//            MeetingOwnerSlotForSecurityDTO meetingDTO = meetingRepository.findOwnerIdAndSlotIdByMeetingId(meetingId);
//
//            if (meetingDTO == null) {
//                throw new ResourceNotFoundException("Meeting not found with ID: " + meetingId);
//            }
//
//            if (!meetingDTO.ownerId().equals(currentUserId)) {
//                throw new AccessDeniedException("You don't have permission to access this meeting");
//            }
//
//            if (meetingDTO.slotId() != null) {
//                if (meetingDTO.slotId().equals(slotId)) {
//                    throw new AccessDeniedException("Same Meeting already assigned to this slot");
//                }
//                throw new AccessDeniedException("Meeting already assigned to another slot");
//            }
//
//            SlotMeetingSecurityCheckDTO slot = slotRepository.findSlotLockAndMeeting(slotId);
//
//            if (slot == null) {
//                throw new ResourceNotFoundException("Slot not found with ID: " + slotId);
//            }
//
//            if (slot.meetingId() != null) {
//                if (slot.meetingId().equals(meetingId)) {
//                    throw new AccessDeniedException("Same Meeting already assigned to this slot");
//                }
//                throw new AccessDeniedException("Meeting already assigned to this slot");
//            }
//
//            if (!slotRepository.isUserVisibleToSlot(slotId, currentUserId)) {
//                throw new AccessDeniedException("You don't have permission to access this slot");
//            }
//        });
//    }
//
//    public void hasPermissionToUnassignSlot(Long meetingId) {
//        ExceptionWrapper.handleVoid(() -> {
//            Long currentUserId = JwtUtil.getUserIdFromJWT();
//            MeetingOwnerSlotForSecurityDTO meetingDTO = meetingRepository.findOwnerIdAndSlotIdByMeetingId(meetingId);
//            if (meetingDTO == null) {
//                throw new ResourceNotFoundException("Meeting not found with ID: " + meetingId);
//            }
//            if (!slotRepository.isUserVisibleToSlot(meetingDTO.slotId(), currentUserId)) {
//                throw new AccessDeniedException("You don't have permission to access this slot");
//            }
//            if (meetingDTO.slotId() == null) {
//                throw new AccessDeniedException("No slot assigned to this meeting");
//            }
//            if (!meetingDTO.ownerId().equals(currentUserId)) {
//                throw new AccessDeniedException("You don't have permission to access this meeting");
//            }
//        });
//    }
//
//    public void hasPermissionToDeleteParticipant(Long participantId) {
//        ExceptionWrapper.handleVoid(() -> {
//            Long currentUserId = commonQueryService.resolveParent();
//            Long participantHostId = participantRepository.findHostIdByParticipantId(participantId);
//            if (!participantHostId.equals(currentUserId)) {
//                throw new AccessDeniedException("You do not have permission to delete this participant");
//            }
//        });
//    }
//
    private boolean isEligibleForTheTask(Long childUserId) {
        ParentInfo parentInfo = getParentInfo(childUserId);
        return isParent(parentInfo);
    }

    private ParentInfo getParentInfo(Long childUserId) {
        RoleType role = userRepository.findRoleById(childUserId);
        return switch (role) {
            case MASTER -> masterRepository.findParentInfoByMasterId(childUserId);
            case SUBSCRIBER -> subscriberRepository.findParentInfoByHostId(childUserId);
            case SUPER_MASTER -> new ParentInfo(superMasterRepository.findOwnerById(childUserId),RoleType.OWNER);
            case MANAGER -> managerRepository.findParentInfoByManagerId(childUserId);
            default -> throw new UnsupportedOperationException("Role " + role + " does not support parent lookup");
        };
    }

    private boolean isParent(ParentInfo parentInfo) {
        Long currentUserId = JwtUtil.getUserIdFromJWT();
        RoleType currentUserRole = RoleType.valueOf(JwtUtil.getRoleFromJWT());

        return parentInfo.parentId().equals(currentUserId) &&
                parentInfo.parentType() == currentUserRole;
    }
}
