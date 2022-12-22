package io.github.adainish.clandorus.obj.clan.data;

import io.github.adainish.clandorus.enumeration.AuditType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuditLog {
    public List<Log> logList = new ArrayList<>();

    public AuditLog(){}

    //add new log
    public void createLog(UUID uuid, String message,  AuditType auditType)
    {
        Log log = new Log(uuid, auditType, message);
        logList.add(0, log);
    }



    //view log ui

    //remove log
    public void removeFromLog(Log log)
    {
        logList.remove(log);
    }

    //clean outdated logs
    public void clearLogs()
    {
        List<Log> toRemove = new ArrayList<>();
        for (Log log:logList) {
            if (log.time > 2) //upgrade to utilise timer system which pulls from config
                toRemove.add(log);
        }
        this.logList.removeAll(toRemove);
    }
}
