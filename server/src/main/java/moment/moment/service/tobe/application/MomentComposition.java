package moment.moment.service.tobe.application;

import moment.moment.dto.response.tobe.MomentCompositions;

@FunctionalInterface
public interface MomentComposition {
    MomentCompositions generate();
}
