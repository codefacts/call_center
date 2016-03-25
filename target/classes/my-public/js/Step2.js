site.reactjs.Step2 = React.createClass({
    getDefaultProps: function () {
        return {
            onInit: function () {
            },
            user: {}
        }
    },
    getInitialState: function () {
        console.log("init Filter-2")
        var $this = this;
        var filterFieldsScope = function () {
            var scope = null;
            return {
                set: function (state) {
                    scope = state;
                },
                get: function () {
                    return scope;
                }
            };
        };
        console.log("INIT>>>")
        return {
            filterFieldsRef: null,
            primaryTableRef: null,
            filterFields: <site.reactjs.FilterFields2 formId="filter-form" scope={filterFieldsScope()}
                                                      onFilterFiledsInit={$this.onFilterFiledsInit}/>,
            ViewFilter: 1,
            brSummary: {total: {}, daily: {}},
            brInfo: {}
        };
    },
    componentDidMount: function () {
        var $this = this;
        var params = site.hash.params() | {};
        $this.updateData(params);
        $this.props.onInit($this);
    },
    render: function () {
        var $this = this;
        var user = $this.props.user;
        var brInfo = $this.state.brInfo;
        var params = site.hash.params() | {};

        return (
            <div className="row">
                <div className="col-md-12">

                </div>
                <div className="col-md-12">
                    <div className="row">

                        <div className="col-md-12">
                            <strong>BR Name: {brInfo.BR_NAME} | BR ID: {brInfo.BR_ID} </strong>
                        </div>

                        <div className="col-md-12">

                            <site.reactjs.DERBY_JAN_2016.BrSummary data={$this.state.brSummary}/>

                        </div>

                        <div id="container" className="col-md-12">

                            <site.reactjs.DERBY_JAN_2016.WorkDayDetailsTable user={user}
                                                                             onInit={$this.onPrimaryTableInit}/>

                        </div>

                        {(function () {
                            if ($this.isViewFilterArrow()) {
                                return (<site.reactjs.FilterArrow onClick={$this.onFilterClick}/>);
                            } else if ($this.isViewFilter()) {
                                return (<site.reactjs.Filter onHeaderClick={$this.onFilterClick}
                                                             onSubmitButtonClick={$this.updateQueryString}
                                                             onClearButtonClick={$this.clearQueryString}
                                                             body={$this.state.filterFields}/>);
                            }
                        })()}

                    </div>
                </div>
            </div>
        );
    },
    onFilterFiledsInit: function (filterFieldsRef) {
        this.setState({filterFieldsRef: filterFieldsRef});
    },
    onPrimaryTableInit: function (primaryTableRef) {
        this.setState({primaryTableRef: primaryTableRef});
    },
    updateQueryString: function () {
        site.hash.setParams($('#filter-form').serializeArray());
    },
    updateData: function (params) {
        var $this = this;
        if (!!$this.state.filterFieldsRef) $this.state.filterFieldsRef.updateFields(params);
        $.ajax({
            url: '/consumer-contacts/call-step-2',
            data: params,
            success: function (js) {
                $this.state.primaryTableRef.updateData(js.data || []);
            },
        });
        $this.updateBrSummary(params);
        $this.updateBrInfo(params);
    },
    updateBrInfo: function (params) {
        var $this = this;

        var brId = parseInt(params.brId);
        brId = !!brId ? brId : 0;
        $.ajax({
            url: '/brs/br-info',
            data: {
                'brId': brId,
            },
            success: function (brInfoJS) {
                $this.setState({
                    brInfo: brInfoJS
                });
            }
        });
    },
    updateBrSummary: function (params) {
        var $this = this;

        $.ajax({
            url: '/br-activity-summary',
            data: {
                'brId': params.brId,
                'workDate': params.workDate,
            },
            success: function (js) {
                $this.state.brSummary.daily = js.daily;
                $this.state.brSummary.total = js.total;
                $this.state.brSummary.__isPresent = true;
                $this.setState({__render: !$this.state.__render});
            }
        });

    },
    clearQueryString: function () {
        var prms = $('#filter-form').serializeArray().map(function (e) {
            return e.name;
        });
        site.hash.removeAll(prms);
    },
    isViewFilter: function () {
        var $this = this;
        return $this.state.ViewFilter == 2;
    },
    isViewFilterArrow: function () {
        var $this = this;
        return $this.state.ViewFilter == 1;
    },
    onFilterClick: function () {
        var $this = this;
        $this.setState({ViewFilter: $this.state.ViewFilter == 2 ? 1 : 2});
    },
});