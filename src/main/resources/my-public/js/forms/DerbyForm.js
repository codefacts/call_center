site.reactjs.DerbyForm = React.createClass({
    getDefaultProps: function () {
        return {
            form: {},
            onInit: function () {
            },
            onValueChange: function () {
            },
            brands: []
        };
    },
    getInitialState: function () {
        return {};
    },
    componentDidMount: function () {
        this.props.onInit(this);
    },
    clearForm: function () {

    },
    render: function () {
        var $this = this;
        var brands = this.props.brands;
        var form = this.props.form;
        var data = this.props.data;
        var onValueChange = this.props.onValueChange;

        return (
            <div className="well well-sm">
                <div className="row contact-details-row">
                    <div className="col-md-3">
                        <label className="checkbox-inline call_label_inline">
                            <input className="call_checkbox" type="checkbox" id="inlineCheckbox1"
                                   name="name_matched"
                                   checked={form.name_matched}
                                   onChange={onValueChange}/>
                            <span className="call_checkbox_label">Father Name Match</span>
                        </label>
                    </div>
                    <div className="col-md-3">
                        <label className="checkbox-inline call_label_inline">
                            <input className="call_checkbox" type="checkbox" id="inlineCheckbox1"
                                   name="br_contact"
                                   checked={form.br_contact}
                                   onChange={onValueChange}/>
                            <span className="call_checkbox_label">BR Contact</span>
                        </label>
                    </div>
                    <div className="col-md-3">
                        <label className="checkbox-inline call_label_inline">
                            <input className="call_checkbox" type="checkbox" id="inlineCheckbox1"
                                   name="target_brand"
                                   checked={form.target_brand}
                                   onChange={onValueChange}/>
                            <span className="call_checkbox_label">Target Brand</span>
                        </label>
                    </div>
                    <div className="col-md-3">
                        <label className="checkbox-inline call_label_inline">
                            <input className="call_checkbox" type="checkbox" id="IS_TARGET_BRAND_RECALLED"
                                   name="IS_TARGET_BRAND_RECALLED"
                                   checked={form.IS_TARGET_BRAND_RECALLED}
                                   onChange={onValueChange}/>
                            <span className="call_checkbox_label">Is Recalled</span>
                        </label>
                    </div>
                </div>

                <div className="row contact-details-row">
                    <div className="col-md-3">
                        <label className="checkbox-inline call_label_inline">
                            <input className="call_checkbox" type="checkbox" id="inlineCheckbox1"
                                   name="video_and_apps_shown"
                                   checked={form.video_and_apps_shown}
                                   onChange={onValueChange}/>
                            <span className="call_checkbox_label">Video Shown</span>
                        </label>
                    </div>
                    <div className="col-md-3">
                        <label className="checkbox-inline call_label_inline">
                            <input className="call_checkbox" type="checkbox" id="inlineCheckbox1"
                                   name="toolkit_shown"
                                   checked={form.toolkit_shown}
                                   onChange={onValueChange}/>
                            <span className="call_checkbox_label">Toolkit Shown</span>
                        </label>
                    </div>
                    <div className="col-md-3">

                        <select className="form-control call-control"
                                name="ptr"
                                value={form.ptr}
                                onChange={onValueChange}>
                            <option value={''}>PTR</option>
                            {
                                [{key: 1, value: 'Yes'}, {key: 0, value: 'No'}].map(function (obj) {
                                    return (<option key={obj.key}
                                                    value={obj.key}>{obj.value}</option>);
                                })
                            }
                        </select>

                    </div>
                    <div className="col-md-3">
                        <label className="checkbox-inline call_label_inline">
                            <input className="call_checkbox" type="checkbox" id="inlineCheckbox1"
                                   name="swp"
                                   checked={form.swp}
                                   onChange={onValueChange}/>
                            <span className="call_checkbox_label">SWP</span>
                        </label>
                    </div>
                </div>

                <div className="row contact-details-row">
                    <div className="col-md-3">
                        <label className="checkbox-inline call_label_inline">
                            <input className="call_checkbox" type="checkbox" id="inlineCheckbox1"
                                   name="REFRESHMENT"
                                   checked={form.REFRESHMENT}
                                   onChange={onValueChange}/>
                            <span className="call_checkbox_label">Refreshment</span>
                        </label>
                    </div>
                    <div className="col-md-3">
                        <label className="checkbox-inline call_label_inline">
                            <input className="call_checkbox" type="checkbox" id="inlineCheckbox1"
                                   name="GIVE_AWAY"
                                   checked={form.GIVE_AWAY}
                                   onChange={onValueChange}/>
                            <span className="call_checkbox_label">Give Away</span>
                        </label>
                    </div>
                    <div className="col-md-3">
                        <label className="checkbox-inline call_label_inline">
                            <input className="call_checkbox" type="checkbox" id="inlineCheckbox1"
                                   name="PACK_SELL"
                                   checked={form.PACK_SELL}
                                   onChange={onValueChange}/>
                            <span className="call_checkbox_label">Pack Sell</span>
                        </label>
                    </div>

                    <div className="col-md-3">
                        <label className="checkbox-inline call_label_inline">
                            <input className="call_checkbox" type="checkbox" id="inlineCheckbox1"
                                   name="dob_matched"
                                   checked={form.dob_matched}
                                   onChange={onValueChange}/>
                            <span className="call_checkbox_label">DOB Matched</span>
                        </label>
                    </div>
                </div>


                <div className="row contact-details-row">
                    <div className="col-md-1">
                        <span className="call-form-label">Brand</span>
                    </div>
                    <div className="col-md-4">
                        <select style={{marginLeft: '10px'}} className="form-control call-control"
                                name="current_brand"
                                value={form.current_brand}
                                onChange={onValueChange}>
                            <option value="">Select Brand</option>
                            {
                                brands.map(function (brand) {
                                    return (<option key={brand.BRAND_ID}
                                                    value={brand.BRAND_ID}>{brand.BRAND_NAME}</option>);
                                })
                            }
                        </select>
                    </div>
                    <div className="col-md-3">
                        <span className="call-form-label">Brand Other</span>
                    </div>
                    <div className="col-md-4">
                        <input type="text" className="form-control"
                               name="brand_other"
                               value={form.brand_other}
                               onChange={onValueChange}/>
                    </div>

                </div>

                <div className="row contact-details-row">
                    <div className="col-md-3">
                        <span className="call-form-label">Age</span>
                    </div>
                    <div className="col-md-9">
                        <input type="number" className="form-control" name="Age"
                               value={form.Age}
                               onChange={onValueChange}/>
                    </div>
                </div>
            </div>
        )
    }
});