site.reactjs.grocery.Step1 = React.createClass({
    getDefaultProps: function () {
        return {
            onInit: function () {
            }
        }
    },
    getInitialState: function () {
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
        console.log("INIT>>>");
        return {
            filterFieldsRef: null,
            primaryTableRef: null,
            filterFields: <site.reactjs.grocery.FilterFields formId="filter-form" scope={filterFieldsScope()}
                                                             onFilterFiledsInit={$this.onFilterFiledsInit}/>,
            ViewFilter: 1
        };
    },
    componentDidMount: function () {
        var $this = this;
        $this.updateData(site.hash.params());
        $this.props.onInit($this);
    },
    render: function () {
        var $this = this;
        return (
            <div className="row">

                <div id="container" className="col-md-12">

                    <site.reactjs.grocery.WorkDayDetailsTable onInit={$this.onPrimaryTableInit}/>

                </div>

                {(function () {
                    if ($this.isViewFilterArrow()) {
                        return (<site.reactjs.grocery.FilterArrow onClick={$this.onFilterClick}/>);
                    } else if ($this.isViewFilter()) {
                        return (<site.reactjs.grocery.Filter onHeaderClick={$this.onFilterClick}
                                                             body={$this.state.filterFields}
                                                             footer={$this.filterFooter()}
                            />);
                    }
                })()}

            </div>
        );
    },
    filterFooter: function () {
        var $this = this;
        return (
            <p className="text-right">
                <button type="button" className="btn btn-danger btn-lg" style={{marginRight: '10px'}}
                        onClick={$this.clearQueryString}>
                    Clear
                </button>
                <button type="button" className="btn btn-primary btn-lg"
                        onClick={$this.updateQueryString}>Submit
                </button>
            </p>
        )
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
            url: 'http://182.160.99.116:6066/contacts',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", 'kasj4asiuai45aiasdfk2adpas');
            },
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            },
            method: 'get',
            data: params,
            cache: false,
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
    }
});