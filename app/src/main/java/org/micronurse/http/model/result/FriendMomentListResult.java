package org.micronurse.http.model.result;

import org.micronurse.model.FriendMoment;

import java.util.List;

public class FriendMomentListResult extends Result{
    private List<FriendMoment> momentList;

    public FriendMomentListResult(int resultCode, String message, List<FriendMoment> momentList){
        super(resultCode, message);
        this.momentList = momentList;
    }

    public List<FriendMoment> getMomentList() {
        return momentList;
    }
}
