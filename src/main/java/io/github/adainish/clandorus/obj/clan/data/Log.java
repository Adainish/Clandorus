package io.github.adainish.clandorus.obj.clan.data;

import io.github.adainish.clandorus.enumeration.AuditType;

import java.util.UUID;

public class Log {
    public UUID uuid;
    public long time;

    public AuditType auditType;

    public String message;

    public Log()
    {

    }

    public Log(UUID uuid, AuditType auditType, String message)
    {
        this.uuid = uuid;
        this.auditType = auditType;
        this.message = message;
        time = System.currentTimeMillis();
    }
}
