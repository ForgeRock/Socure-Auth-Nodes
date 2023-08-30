////////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2022 Socure Inc.
// All rights reserved.
////////////////////////////////////////////////////////////////////////////////

package me.socure.custom.node.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocvCallbackVO {
    private String eventId;
    private String status;
    private int verificationLevel;
    private String key;
    private String referenceId;
    private String documentUuid;

}

