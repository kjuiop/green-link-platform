package com.greenlink.shopcore.application.onurim.usecase;

import com.greenlink.shopcore.application.common.ManagementActor;
import com.greenlink.shopcore.application.onurim.dto.OnurimApplicationView;
import com.greenlink.shopcore.application.onurim.dto.OnurimApplyCommand;
import com.greenlink.shopcore.application.onurim.dto.OnurimDocumentRequirement;

import java.util.Optional;

public interface OnurimApplicationUseCase {

    OnurimApplicationView apply(ManagementActor actor, OnurimApplyCommand command);

    Optional<OnurimApplicationView> getLatest(ManagementActor actor);

    OnurimDocumentRequirement getDocumentRequirement(ManagementActor actor);
}
