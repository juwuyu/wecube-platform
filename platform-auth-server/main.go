package main

import (
	"flag"
	"fmt"
	sw "github.com/WeBankPartners/wecube-platform/platform-auth-server/api"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
)

func main() {
	configFile := flag.String("c", "config/default.json", "config file path")
	flag.Parse()
	if initConfigMessage := model.InitConfig(*configFile); initConfigMessage != "" {
		fmt.Printf("Init config file error,%s \n", initConfigMessage)
		return
	}

	log.InitLogger()
	/*	middleware.InitAuth()
		middleware.InitRedirect()
	*/
	/*	err := exterror.InitErrorTemplateList(config.Config.ErrorTemplateDir, config.Config.ErrorDetailReturn)
		if err != nil {
			log.Logger.Error("Init error template list fail", log.Error(err))
			return
		}
	*/
	log.Logger.Info("Server started")

	router := sw.NewRouter()
	go router.Run(":" + model.Config.Port)

}
