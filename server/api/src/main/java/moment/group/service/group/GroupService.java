package moment.group.service.group;

import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.group.domain.Group;
import moment.group.infrastructure.GroupRepository;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {

    private final GroupRepository groupRepository;

    @Transactional
    public Group create(String name, String description, User owner) {
        Group group = new Group(name, description, owner);
        return groupRepository.save(group);
    }

    public Group getById(Long id) {
        return groupRepository.findById(id)
            .orElseThrow(() -> new MomentException(ErrorCode.GROUP_NOT_FOUND));
    }

    @Transactional
    public void update(Long groupId, String name, String description, User requester) {
        Group group = getById(groupId);
        validateOwner(group, requester);
        group.updateInfo(name, description);
    }

    @Transactional
    public void delete(Long groupId, User requester) {
        Group group = getById(groupId);
        validateOwner(group, requester);
        groupRepository.delete(group);
    }

    private void validateOwner(Group group, User user) {
        if (!group.isOwner(user)) {
            throw new MomentException(ErrorCode.NOT_GROUP_OWNER);
        }
    }
}
