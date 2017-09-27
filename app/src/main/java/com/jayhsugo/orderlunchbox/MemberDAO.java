package com.jayhsugo.orderlunchbox;

import java.util.List;

/**
 * Created by Administrator on 2017/8/30.
 */

public interface MemberDAO {
    String insert(Member member);
    Member findByUserid(String name);
    List<Member> getAll();
}
