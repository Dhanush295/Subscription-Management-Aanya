package com.netflixsub.model;

import com.netflixsub.plan.Plan;
import java.sql.*;
import java.util.*;

public class PlanRepository {
    public List<Plan> all() {
        List<Plan> out = new ArrayList<>();
        try (Statement s = Db.get().createStatement();
             ResultSet rs = s.executeQuery("SELECT code,name,monthly_inr,yearly_inr,screens,quality,trial_days,yearly_discount_pct FROM plans ORDER BY monthly_inr")) {
            while (rs.next()) {
                out.add(new Plan(rs.getString(1), rs.getString(2),
                        rs.getDouble(3), rs.getDouble(4),
                        rs.getInt(5), rs.getString(6),
                        rs.getInt(7), rs.getInt(8)));
            }
            return out;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
