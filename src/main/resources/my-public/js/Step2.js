site.reactjs.Step2 = React.createClass({
    getDefaultProps: function () {
        return {
            onInit: function () {
            }
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
            ViewFilter: 1
        };
    },
    componentDidMount: function () {
        var $this = this;
        $this.updateData();
        $this.props.onInit($this);
    },
    render: function () {
        var $this = this;
        return (
            <div className="row">

                <div id="container" className="col-md-12">

                    <site.reactjs.WorkDayDetailsTable onInit={$this.onPrimaryTableInit}/>

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