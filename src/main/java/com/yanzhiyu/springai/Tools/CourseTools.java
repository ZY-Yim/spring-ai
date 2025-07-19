package com.yanzhiyu.springai.Tools;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.yanzhiyu.springai.entity.pojo.Course;
import com.yanzhiyu.springai.entity.pojo.CourseReservation;
import com.yanzhiyu.springai.entity.pojo.School;
import com.yanzhiyu.springai.query.CourseQuery;
import com.yanzhiyu.springai.service.ICourseReservationService;
import com.yanzhiyu.springai.service.ICourseService;
import com.yanzhiyu.springai.service.ISchoolService;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author yanzhiyu
 * @date 2025/7/6
 */
@Component
public class CourseTools {

    @Resource
    private ICourseService courseService;

    @Resource
    private ISchoolService schoolService;

    @Resource
    private ICourseReservationService courseReservationService;

    // 结果返回给大模型
    @Tool(description = "根据条件查询课程", returnDirect = false)
    public List<Course> queryCourse(@ToolParam(description = "查询条件", required = false) CourseQuery query) {
        if (query == null) {
            return List.of();
        }
        QueryChainWrapper<Course> wrapper = courseService.query()
                .eq(query.getType() != null, "type", query.getType())
                // edu<=2
                .le(query.getEdu() != null, "edu", query.getEdu());
        if (query.getSorts() != null && !query.getSorts().isEmpty()) {
            for (CourseQuery.Sort sort : query.getSorts()) {
                wrapper.orderBy(true, sort.getAsc(), sort.getField());
            }
        }
        return wrapper.list();
    }

    @Tool(description = "查询所有校区", returnDirect = false)
    public List<School> querySchool() {
        return schoolService.list();
    }

    @Tool(description = "创建课程预约,返回预约单号", returnDirect = false)
    public Integer createCourseReservation(@ToolParam(description = "课程id") String course,
                                           @ToolParam(description = "校区id") String school,
                                           @ToolParam(description = "预约人姓名") String studentName,
                                           @ToolParam(description = "预约人联系方式") String contactInfo,
                                           @ToolParam(description = "备注", required = false) String remark) {
        CourseReservation courseReservation = new CourseReservation();
        courseReservation.setCourse(course);
        courseReservation.setSchool(school);
        courseReservation.setStudentName(studentName);
        courseReservation.setContactInfo(contactInfo);
        courseReservation.setRemark(remark);
        courseReservationService.save(courseReservation);

        return courseReservation.getId();
    }
}
