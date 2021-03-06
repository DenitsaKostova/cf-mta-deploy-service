package com.sap.cloud.lm.sl.cf.core.dto.serialization.v1;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.sap.cloud.lm.sl.mta.model.v1_0.Target;

@XmlAccessorType(value = javax.xml.bind.annotation.XmlAccessType.FIELD)
@XmlRootElement(name = "target-platforms")
@Deprecated
public class TargetPlatformsDto extends com.sap.cloud.lm.sl.cf.core.dto.serialization.TargetPlatformsDto {

    @XmlJavaTypeAdapter(TargetPlatformAdapter.class)
    @XmlElement(name = "target-platform")
    private List<Target> deployTargets;

    public TargetPlatformsDto() {
        // Required by JAXB
    }

    public TargetPlatformsDto(List<Target> deployTargets) {
        this.deployTargets = deployTargets;
    }

    @Override
    public List<Target> getTargetPlatforms() {
        return deployTargets;
    }
}

class TargetPlatformAdapter extends XmlAdapter<TargetPlatformDto, Target> {

    @Override
    public Target unmarshal(TargetPlatformDto dto) throws Exception {
        return dto.toTargetPlatform();
    }

    @Override
    public TargetPlatformDto marshal(Target target) throws Exception {
        return new TargetPlatformDto(target);
    }

}