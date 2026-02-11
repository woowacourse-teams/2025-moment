package moment.moment.service.application;

import moment.moment.dto.response.tobe.MomentCompositions;

@FunctionalInterface
public interface MomentComposition {
    MomentCompositions generate();
}
