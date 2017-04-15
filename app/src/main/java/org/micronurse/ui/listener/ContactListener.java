package org.micronurse.ui.listener;

import org.micronurse.model.User;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-23
 */
public interface ContactListener {
    void onAddGuardianship(User newContact);
    void onAddFriend(User newContact);
}
