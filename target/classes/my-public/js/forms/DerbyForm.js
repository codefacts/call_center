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

                        <select className="form-control call-control"
                                name="name_matched"
                                value={form.name_matched}
                                onChange={onValueChange}>
                            <option value={''}>Name Match</option>
                            {
                                [{key: 1, value: 'Yes'}, {key: 0, value: 'No'}].map(function (obj) {
                                    return (<option key={obj.key}
                                                    value={obj.key}>{obj.value}</option>);
                                })
                            }
                        </select>
                    </div>
                    <div className="col-md-3">

                        <select className="form-control call-control"
                                name="br_contact"
                                value={form.br_contact}
                                onChange={onValueChange}>
                            <option value={''}>BR Contact</option>
                            {
                                [{key: 1, value: 'Yes'}, {key: 0, value: 'No'}].map(function (obj) {
                                    return (<option key={obj.key}
                                                    value={obj.key}>{obj.value}</option>);
                                })
                            }
                        </select>

                    </div>
                    <div className="col-md-3">

                        <select className="form-control call-control"
                                name="target_brand"
                                value={form.target_brand}
                                onChange={onValueChange}>
                            <option value={''}>Target Brand</option>
                            {
                                [{key: 1, value: 'Yes'}, {key: 0, value: 'No'}].map(function (obj) {
                                    return (<option key={obj.key}
                                                    value={obj.key}>{obj.value}</option>);
                                })
                            }
                        </select>

                    </div>
                    <div className="col-md-3">

                        <select className="form-control call-control"
                                name="IS_TARGET_BRAND_RECALLED"
                                value={form.IS_TARGET_BRAND_RECALLED}
                                onChange={onValueChange}>
                            <option value={''}>Is Recalled</option>
                            {
                                [{key: 1, value: 'Yes'}, {key: 0, value: 'No'}].map(function (obj) {
                                    return (<option key={obj.key}
                                                    value={obj.key}>{obj.value}</option>);
                                })
                            }
                        </select>

                    </div>
                </div>

                <div className="row contact-details-row">
                    <div className="col-md-3">

                        <select className="form-control call-control"
                                name="video_and_apps_shown"
                                value={form.video_and_apps_shown}
                                onChange={onValueChange}>
                            <option value={''}>Video Shown</option>
                            {
                                [{key: 1, value: 'Yes'}, {key: 0, value: 'No'}].map(function (obj) {
                                    return (<option key={obj.key}
                                                    value={obj.key}>{obj.value}</option>);
                                })
                            }
                        </select>

                    </div>
                    <div className="col-md-3">

                        <select className="form-control call-control"
                                name="toolkit_shown"
                                value={form.toolkit_shown}
                                onChange={onValueChange}>
                            <option value={''}>Toolkit Shown</option>
                            {
                                [{key: 1, value: 'Yes'}, {key: 0, value: 'No'}].map(function (obj) {
                                    return (<option key={obj.key}
                                                    value={obj.key}>{obj.value}</option>);
                                })
                            }
                        </select>

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

                        <select className="form-control call-control"
                                name="swp"
                                value={form.swp}
                                onChange={onValueChange}>
                            <option value={''}>SWP</option>
                            {
                                [{key: 1, value: 'Yes'}, {key: 0, value: 'No'}].map(function (obj) {
                                    return (<option key={obj.key}
                                                    value={obj.key}>{obj.value}</option>);
                                })
                            }
                        </select>

                    </div>
                </div>

                <div className="row contact-details-row">
                    <div className="col-md-3">

                        <select className="form-control call-control"
                                name="REFRESHMENT"
                                value={form.REFRESHMENT}
                                onChange={onValueChange}>
                            <option value={''}>Refreshment</option>
                            {
                                [{key: 1, value: 'Yes'}, {key: 0, value: 'No'}].map(function (obj) {
                                    return (<option key={obj.key}
                                                    value={obj.key}>{obj.value}</option>);
                                })
                            }
                        </select>

                    </div>
                    <div className="col-md-3">

                        <select className="form-control call-control"
                                name="GIVE_AWAY"
                                value={form.GIVE_AWAY}
                                onChange={onValueChange}>
                            <option value={''}>Give Away</option>
                            {
                                [{key: 1, value: 'Yes'}, {key: 0, value: 'No'}].map(function (obj) {
                                    return (<option key={obj.key}
                                                    value={obj.key}>{obj.value}</option>);
                                })
                            }
                        </select>

                    </div>
                    <div className="col-md-3">

                        <select className="form-control call-control"
                                name="PACK_SELL"
                                value={form.PACK_SELL}
                                onChange={onValueChange}>
                            <option value={''}>Pack Sell</option>
                            {
                                [{key: 1, value: 'Yes'}, {key: 0, value: 'No'}].map(function (obj) {
                                    return (<option key={obj.key}
                                                    value={obj.key}>{obj.value}</option>);
                                })
                            }
                        </select>

                    </div>

                    <div className="col-md-3">

                        <select className="form-control call-control"
                                name="dob_matched"
                                value={form.dob_matched}
                                onChange={onValueChange}>
                            <option value={''}>DOB Matched</option>
                            {
                                [{key: 1, value: 'Yes'}, {key: 0, value: 'No'}].map(function (obj) {
                                    return (<option key={obj.key}
                                                    value={obj.key}>{obj.value}</option>);
                                })
                            }
                        </select>

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