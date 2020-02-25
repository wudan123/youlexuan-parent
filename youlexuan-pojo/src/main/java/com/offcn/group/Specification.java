package com.offcn.group;

import com.offcn.pojo.TbSpecification;
import com.offcn.pojo.TbSpecificationOption;

import java.io.Serializable;
import java.util.List;

public class Specification implements Serializable {

    private TbSpecification specification;//规格

    private List<TbSpecificationOption> optionList;//规格选项

    public Specification() {
        super();
    }

    public Specification(TbSpecification specification, List<TbSpecificationOption> optionList) {
        super();
        this.specification = specification;
        this.optionList = optionList;
    }

    public TbSpecification getSpecification() {
        return specification;
    }

    public void setSpecification(TbSpecification specification) {
        this.specification = specification;
    }

    public List<TbSpecificationOption> getOptionList() {
        return optionList;
    }

    public void setOptionList(List<TbSpecificationOption> optionList) {
        this.optionList = optionList;
    }


}
