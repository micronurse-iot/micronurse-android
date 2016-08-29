package org.micronurse.database.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by zhou-shengyun on 8/29/16.
 */

@Table(name = "Guardianship")
public class Guardianship extends Model {
    @Column(name = "GuardianID", notNull = true)
    private String guardianId;

    @Column(name = "OlderID", notNull = true)
    private String olderId;

    public Guardianship(){
        super();
    }

    public Guardianship(String guardianId, String olderId) {
        super();
        this.guardianId = guardianId;
        this.olderId = olderId;
    }

    public String getGuardianId() {
        return guardianId;
    }

    public void setGuardianId(String guardianId) {
        this.guardianId = guardianId;
    }

    public String getOlderId() {
        return olderId;
    }

    public void setOlderId(String olderId) {
        this.olderId = olderId;
    }
}
