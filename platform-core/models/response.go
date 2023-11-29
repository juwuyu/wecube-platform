package models

type PageInfo struct {
	StartIndex int `json:"startIndex"`
	PageSize   int `json:"pageSize"`
	TotalRows  int `json:"totalRows"`
}

type ResponsePageData struct {
	PageInfo PageInfo    `json:"pageInfo"`
	Contents interface{} `json:"contents"`
}

type ResponseJson struct {
	HttpResponseMeta
	Data interface{} `json:"data"`
}

type HttpResponseMeta struct {
	Code    int    `json:"code"`
	Status  string `json:"status"`
	Message string `json:"message"`
}

type SysLogTable struct {
	LogCat      string `json:"logCat" xorm:"log_cat"`
	Operator    string `json:"operator" xorm:"operator"`
	Operation   string `json:"operation" xorm:"operation"`
	Content     string `json:"content" xorm:"content"`
	RequestUrl  string `json:"requestUrl" xorm:"request_url"`
	ClientHost  string `json:"clientHost" xorm:"client_host"`
	CreatedDate string `json:"createdDate" xorm:"created_date"`
	DataCiType  string `json:"dataCiType" xorm:"data_ci_type"`
	DataGuid    string `json:"dataGuid" xorm:"data_guid"`
	DataKeyName string `json:"dataKeyName" xorm:"data_key_name"`
	Response    string `json:"response" xorm:"response"`
}
