package com.salesinsight.infra.ai;

import com.salesinsight.meeting.domain.Insight;
import com.salesinsight.meeting.domain.Meeting;

public interface InsightGeneratorService {

    Insight generate(Meeting meeting);

}
